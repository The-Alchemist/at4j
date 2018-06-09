/* AT4J -- Archive file tools for Java -- http://www.at4j.org
 * Copyright (C) 2009 Karl Gustafsson
 *
 * This file is a part of AT4J
 *
 * AT4J is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * AT4J is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.at4j.comp.bzip2;

import java.io.IOException;
import java.io.InputStream;

import org.at4j.support.io.LittleEndianBitInputStream;
import org.entityfs.support.log.LogAdapter;

/**
 * This is an {@link InputStream} for reading bzip2 encoded data.
 * <p>
 * For more information on the inner workings of bzip2, see <a
 * href="http://en.wikipedia.org/wiki/Bzip2">the Wikipedia article on bzip2</a>.
 * <p>
 * This stream is <i>not</i> safe for concurrent access by several writing
 * threads. A client must provide external synchronization to use this from
 * several threads.
 * @author Karl Gustafsson
 * @since 1.1
 * @see BZip2InputStreamSettings
 * @see BZip2OutputStream
 */
public class BZip2InputStream extends InputStream
{
	private final LittleEndianBitInputStream m_in;
	// May be null
	private final LogAdapter m_logAdapter;
	// 1-9*100 kbytes.
	private final int m_blockSize;
	// A decoder for reading the blocks of compressed data
	private final BlockDecoder m_blockDecoder;

	// This checksum is calculated by combining the checksums of all data blocks
	private int m_fileChecksum = 0;
	private int m_curBlockNo = 0;
	private int m_curBlockChecksumExpected;
	private CRC m_curBlockChecksumCalculated = new CRC();
	private boolean m_atEof = false;
	private boolean m_closed = false;
	private InputStream m_curBlock;

	/**
	 * Read the file header and return the block size.
	 */
	private static int readFileHeader(LittleEndianBitInputStream in, LogAdapter la) throws IOException
	{
		byte[] barr = new byte[2];
		int noRead = in.read(barr, 0, 2);
		if (noRead != 2)
		{
			throw new IOException("Wanted to read two bytes. Got " + noRead);
		}
		if (barr[0] != 'B' || barr[1] != 'Z')
		{
			throw new IOException("The stream does not start with the magic bytes BZ. Got " + (char) barr[0] + (char) barr[1]);
		}

		if (la != null)
		{
			la.logTrace("Read bzip2 stream header OK");
		}
		int streamVersion = in.read();
		if (streamVersion != 'h')
		{
			throw new IOException("Unsupported bzip2 stream version " + streamVersion + ". The only version supported is 'h' (104)");
		}
		if (la != null)
		{
			la.logTrace("Read bzip2 stream version OK");
		}

		int blockSizeRead = in.read();
		if (!Character.isDigit(blockSizeRead))
		{
			throw new IOException("Invalid block size " + blockSizeRead + ". Expected a character in the range 1..9");
		}
		int blockSize = (blockSizeRead - 48);
		if ((blockSize < 1 || blockSize > 9))
		{
			throw new IOException("Invalid block size " + blockSizeRead + ". Expected a character in the range 1..9");
		}

		if (la != null)
		{
			la.logTrace("Read bzip2 block size " + blockSize + " * 100kb");
		}

		// Return a block size 100 - 900 kb
		// bzip2 uses 1kb == 1000 bytes (not 1024)
		return blockSize * 100 * 1000;
	}

	/**
	 * Create a new bzip2 input stream that will read compressed data from
	 * {@code in}.
	 * @param in The stream to read compressed data from.
	 * @throws IOException On errors when reading the file header.
	 * @see #BZip2InputStream(InputStream, BZip2InputStreamSettings)
	 */
	public BZip2InputStream(InputStream in) throws IOException
	{
		this(in, null);
	}

	/**
	 * Create a new bzip2 input stream that will read compressed data from
	 * {@code in}.
	 * @param in The stream to read compressed data from.
	 * @param settings Settings for the decoder.
	 * @throws IOException On errors when reading the file header.
	 * @see #BZip2InputStream(InputStream)
	 */
	public BZip2InputStream(InputStream in, BZip2InputStreamSettings settings) throws IOException
	{
		// Null check
		in.getClass();

		m_in = new LittleEndianBitInputStream(in);
		// May be null
		m_logAdapter = settings != null ? settings.getLogAdapter() : null;
		m_blockSize = readFileHeader(m_in, m_logAdapter);
		m_blockDecoder = new BlockDecoder(m_in, m_blockSize, m_logAdapter);
	}

	/**
	 * Read a new bzip2 block and return a stream for reading decoded data from
	 * it.
	 * @return A stream to read data from, or {@code null} if the end of the
	 * file has been reached.
	 */
	private InputStream readNewBlock() throws IOException
	{
		Block b = m_blockDecoder.getNextBlock();
		if (b instanceof CompressedDataBlock)
		{
			m_curBlockNo++;

			m_curBlockChecksumCalculated = new CRC();
			m_curBlockChecksumExpected = ((CompressedDataBlock) b).getBlockChecksum();

			return ((CompressedDataBlock) b).getStream();
		}
		else if (b instanceof EosBlock)
		{
			if (m_fileChecksum != ((EosBlock) b).getReadCrc())
			{
				throw new IOException("Invalid file checksum " + m_fileChecksum + ". Expected " + ((EosBlock) b).getReadCrc());
			}
			m_atEof = true;
			return null;
		}
		else
		{
			throw new RuntimeException("Unknown bzip2 block type " + b + ". This is a bug");
		}
	}

	private void assertNotClosed() throws IOException
	{
		if (m_closed)
		{
			throw new IOException("This stream is closed");
		}
	}

	/**
	 * Verify the block checksum.
	 * @throws IOException If the checksum is invalid.
	 */
	private void verifyBlockChecksum() throws IOException
	{
		if (m_curBlockChecksumCalculated.getValue() != m_curBlockChecksumExpected)
		{
			throw new IOException("Invalid checksum for bzip2 block no " + m_curBlockNo + ". Was " + m_curBlockChecksumCalculated.getValue() + ". Expected " + m_curBlockChecksumExpected);
		}
	}

	/**
	 * Update the file checksum with the checksum for the current block.
	 * <p>
	 * This method is called after a full block has been read.
	 */
	private void updateFileChecksum()
	{
		// Update the file checksum with the checksum for this block.
		m_fileChecksum = (m_fileChecksum << 1) | (m_fileChecksum >>> 31);
		m_fileChecksum ^= m_curBlockChecksumExpected;
	}

	@Override
	public int read() throws IOException
	{
		assertNotClosed();
		if (m_curBlock == null)
		{
			if (m_atEof)
			{
				return -1;
			}
			m_curBlock = readNewBlock();
			if (m_atEof)
			{
				return -1;
			}
		}

		int res = m_curBlock.read();
		if (res == -1)
		{
			verifyBlockChecksum();
			updateFileChecksum();
			m_curBlock = null;
			// Read from the next block.
			return read();
		}
		else
		{
			m_curBlockChecksumCalculated.update(res);
			return res;
		}
	}

	@Override
	public int read(byte[] barr) throws IOException
	{
		return read(barr, 0, barr.length);
	}

	@Override
	public int read(byte[] barr, int off, int len) throws IOException, IndexOutOfBoundsException
	{
		assertNotClosed();

		if (off < 0)
		{
			throw new IndexOutOfBoundsException("Off: " + off);
		}
		if (len < 0)
		{
			throw new IndexOutOfBoundsException("Len: " + len);
		}
		if (off + len > barr.length)
		{
			throw new IndexOutOfBoundsException("Off: " + off + " + Len: " + len + " > length of array: " + barr.length);
		}

		int totalNoRead = 0;
		while (len > 0)
		{
			if (m_curBlock == null)
			{
				if (m_atEof)
				{
					return totalNoRead == 0 ? -1 : totalNoRead;
				}
				m_curBlock = readNewBlock();
				if (m_atEof)
				{
					return totalNoRead == 0 ? -1 : totalNoRead;
				}
			}

			final int noRead = m_curBlock.read(barr, off, len);

			// Update the checksum
			if (noRead > 0)
			{
				for (int i = off; i < off + noRead; i++)
				{
					m_curBlockChecksumCalculated.update(barr[i] & 0xFF);
				}

				off += noRead;
			}

			if (noRead < len)
			{
				verifyBlockChecksum();
				updateFileChecksum();
				m_curBlock = null;
			}
			totalNoRead += noRead;
			len -= noRead;
		}
		return totalNoRead;
	}

	@Override
	public void close() throws IOException
	{
		if (!m_closed)
		{
			m_in.close();
			m_closed = true;
			super.close();
		}
	}

	/**
	 * Close the stream if the client has been sloppy about it.
	 */
	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
}
