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
package org.at4j.comp.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class provides an {@link java.io.InputStream} for decoding data using
 * the Lempel-Ziv-Markov chain algorithm. It uses the LZMA decoder from <a
 * href="http://www.7-zip.org/sdk.html">the LZMA SDK</a>.
 * <p>
 * The API from the LZMA SDK is built around a standalone decoder. To adapt that
 * into the Java streams API, the decoder is launched in a separate execution
 * thread. The decoder writes data to a blocking queue that this stream reads
 * its data from.
 * <p>
 * Errors are propagated up from the decoder to the calling thread. If an error
 * occurs in the decoder, it will be reported to the calling thread the next
 * time that it tries to read from or close the stream.
 * <p>
 * Decoding a file consumes a lot of memory &ndash; up to the dictionary size
 * set when encoding it multiplied with the decoder data queue depth plus one.
 * (The default dictionary size is 2^23&nbsp;=&nbsp;8.4&nbsp;MB and the default
 * data queue depth is 1.)
 * <p>
 * By default, data in an LZMA stream has the following format:
 * <ol>
 * <li>Settings used when encoding (5 bytes)</li>
 * <li>Total size of data in the stream when uncompressed (8 bytes, little
 * endian). This may be set to -1 if the size of the data was unknown at the
 * time of compression (stream mode). If so, the data part must be followed by
 * an end of stream marker</li>
 * <li>Data</li>
 * <li>End of stream marker (if the total data size was set to -1)</li>
 * </ol>
 * When LZMA compressed data is used in a setting where some of the header data
 * may be known before opening the stream, such as in a Zip file, the settings
 * header may be omitted. When creating this stream, the client may pass in a
 * {@link LzmaInputStreamSettings} object that says which headers the stream can
 * expect to find.
 * @author Karl Gustafsson
 * @since 1.0
 * @see LzmaInputStreamSettings
 * @see LzmaOutputStream
 */
public final class LzmaInputStream extends InputStream
{
	private final Thread m_readerThread;
	private final LzmaErrorState m_errorState;
	private final LinkedBlockingQueue<byte[]> m_dataQueue;
	private final InputStream m_wrapped;

	private boolean m_closed;
	private byte[] m_curBlock;
	private int m_curPosInBlock;

	/**
	 * Create a LZMA decoding stream using the default settings.
	 * @param is The stream containing LZMA encoded data. Since this stream is
	 * read in a separate thread, a lock-aware stream such as one returned from
	 * {@link org.entityfs.util.Files#openForRead(org.entityfs.ReadableFile)}
	 * cannot be used.
	 * @see #LzmaInputStream(InputStream, LzmaInputStreamSettings)
	 * @see LzmaInputStreamSettings
	 */
	public LzmaInputStream(InputStream is)
	{
		this(is, new LzmaInputStreamSettings());
	}

	/**
	 * Create a LZMA decoding stream using custom settings.
	 * @param is The stream containing LZMA encoded data. Since this stream is
	 * read in a separate thread, a lock-aware stream such as one returned from
	 * {@link org.entityfs.util.Files#openForRead(org.entityfs.ReadableFile)}
	 * cannot be used.
	 * @param settings The compression settings that were used when writing the
	 * compressed data.
	 * @throws IllegalArgumentException If the uncompressed size is less than
	 * -1.
	 */
	public LzmaInputStream(InputStream is, LzmaInputStreamSettings settings) throws IllegalArgumentException
	{
		if (settings.getUncompressedSize() < -1)
		{
			throw new IllegalArgumentException("Invalid uncompressed size of data: " + settings.getUncompressedSize());
		}
		m_wrapped = is;
		m_errorState = new LzmaErrorState();
		m_dataQueue = new LinkedBlockingQueue<byte[]>(settings.getMaxDataQueueDepth());
		Runnable decoderRunnable = new LzmaReaderRunnable(is, m_errorState, new LzmaDecoderOutputStream(m_dataQueue), settings);
		m_readerThread = settings.getThreadFactory() != null ? settings.getThreadFactory().newThread(decoderRunnable) : new Thread(decoderRunnable);
		m_readerThread.start();
	}

	private void assertNotClosed() throws IOException
	{
		if (m_closed)
		{
			throw new IOException("This stream is closed");
		}
	}

	private void testAndSetDataBlock() throws IOException
	{
		if ((m_curBlock == null) || (m_curPosInBlock >= m_curBlock.length))
		{
			// Special case: EOF
			if ((m_curBlock != null) && (m_curBlock.length == 0))
			{
				return;
			}

			try
			{
				m_curBlock = m_dataQueue.take();
			}
			catch (InterruptedException e)
			{
				// Reset the thread's interrupt flag
				Thread.currentThread().interrupt();
			}
			m_curPosInBlock = 0;

			// Any new errors?
			m_errorState.testAndClearErrors();
		}
	}

	private boolean isAtEof()
	{
		return m_curBlock.length == 0;
	}

	@Override
	public int read() throws IOException
	{
		byte[] barr = new byte[1];
		int noRead = read(barr, 0, 1);
		return noRead == -1 ? -1 : barr[0] & 0xFF;
	}

	@Override
	public int read(byte[] barr) throws IOException
	{
		return read(barr, 0, barr.length);
	}

	@Override
	public int read(byte[] barr, int off, int len) throws IOException
	{
		assertNotClosed();
		m_errorState.testAndClearErrors();

		int noRead = 0;
		while (len > 0)
		{
			testAndSetDataBlock();
			if (isAtEof() || Thread.currentThread().isInterrupted())
			{
				return noRead == 0 ? -1 : noRead;
			}

			int canReadFromBlock = m_curBlock.length - m_curPosInBlock;
			if (len <= canReadFromBlock)
			{
				// Can read all that we want from the current block
				System.arraycopy(m_curBlock, m_curPosInBlock, barr, off, len);
				m_curPosInBlock += len;
				noRead += len;
				return noRead;
			}
			else
			{
				// Read the rest of the current block and then proceed to the
				// next block.
				if (canReadFromBlock > 0)
				{
					System.arraycopy(m_curBlock, m_curPosInBlock, barr, off, canReadFromBlock);
					off += canReadFromBlock;
					noRead += canReadFromBlock;
					len -= canReadFromBlock;
					m_curBlock = null;
				}
			}
		}
		// Could read all that we wanted
		return len;
	}

	@Override
	public void close() throws IOException
	{
		if (!m_closed)
		{
			try
			{
				try
				{
					m_readerThread.interrupt();
					try
					{
						m_readerThread.join();
					}
					catch (InterruptedException e)
					{
						// Reset this thread's interrupt flag
						Thread.currentThread().interrupt();
					}
				}
				finally
				{
					m_wrapped.close();
				}
			}
			finally
			{
				m_closed = true;
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}
}
