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
 * This input stream is used by the LZMA encoder to read data from. This stream
 * reads its data from a blocking queue shared with the {@link LzmaOutputStream}
 * object. When data is written to that stream, it becomes available in the
 * queue and is passed on to the encoder.
 * <p>
 * A zero length byte array in the data queue is interpreted as EOF.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class LzmaEncoderInputStream extends InputStream
{
	private final LinkedBlockingQueue<byte[]> m_dataQueue;

	private byte[] m_curData;
	private int m_posInCurData;
	private boolean m_closed;

	LzmaEncoderInputStream(LinkedBlockingQueue<byte[]> dataQueue)
	{
		m_dataQueue = dataQueue;
	}

	private void testAndSetCurData()
	{
		if ((m_curData == null) || (m_posInCurData >= m_curData.length))
		{
			// Special case: EOF
			if ((m_curData != null) && (m_curData.length == 0))
			{
				return;
			}

			// Get the next data block from the queue
			try
			{
				m_curData = m_dataQueue.take();
				m_posInCurData = 0;
			}
			catch (InterruptedException e)
			{
				// Hope that the Encoder looks at this flag...
				Thread.currentThread().interrupt();
			}
		}
	}

	private void assertNotClosed() throws IOException
	{
		if (m_closed)
		{
			throw new IOException("This stream is closed");
		}
	}

	private boolean isAtEof()
	{
		return m_curData.length == 0;
	}

	@Override
	public int available() throws IOException
	{
		assertNotClosed();
		testAndSetCurData();
		// If at EOF, m_curData has length 0
		return m_curData.length - m_posInCurData;
	}

	@Override
	public int read() throws IOException
	{
		assertNotClosed();
		testAndSetCurData();
		if (isAtEof())
		{
			return -1;
		}
		else
		{
			return m_curData[m_posInCurData++] & 0xFF;
		}
	}

	@Override
	public int read(byte[] barr) throws IOException
	{
		return read(barr, 0, barr.length);
	}

	private int readInternal(byte[] barr, int off, int length)
	{
		int totalRead = 0;
		while(length > 0)
		{
			int noRemaining = m_curData.length - m_posInCurData;
			if (noRemaining > length)
			{
				// Fill the supplied array
				System.arraycopy(m_curData, m_posInCurData, barr, off + totalRead, length);
				m_posInCurData += length;
				totalRead += length;
				return totalRead;
			}
			else
			{
				// Write as much as possible to the supplied array and then get the
				// next data block from the incoming queue
				System.arraycopy(m_curData, m_posInCurData, barr, off + totalRead, noRemaining);
				totalRead += noRemaining;
				m_curData = null;
				if (noRemaining == length)
				{
					// We're done
					return totalRead;
				}
				length -= noRemaining;

				testAndSetCurData();
				if (isAtEof())
				{
					// No more incoming data. We're done.
					return totalRead;
				}
			}
		}
		return totalRead;
	}

	@Override
	public int read(byte[] barr, int off, int length) throws IOException
	{
		assertNotClosed();
		if (off < 0)
		{
			throw new IndexOutOfBoundsException("Invalid offset " + off);
		}
		else if (length < 0)
		{
			throw new IndexOutOfBoundsException("Invalid length " + length);
		}
		else if (off + length > barr.length)
		{
			throw new IndexOutOfBoundsException("Invalid offset + length " + off + " + " + length + ". Array length " + barr.length);
		}

		if (length == 0)
		{
			return 0;
		}

		testAndSetCurData();

		if (isAtEof())
		{
			return -1;
		}
		else
		{
			return readInternal(barr, off, length);
		}
	}

	@Override
	public long skip(long n) throws IOException
	{
		assertNotClosed();
		long noSkipped = 0;
		while (n > 0)
		{
			testAndSetCurData();
			if (isAtEof())
			{
				return noSkipped;
			}
			else
			{
				if (m_curData.length > (n + m_posInCurData))
				{
					m_posInCurData += (int) n;
					return noSkipped + n;
				}
				else
				{
					noSkipped += m_curData.length - m_posInCurData;
					n -= m_curData.length;
					m_curData = null;
					m_posInCurData = 0;
				}
			}
		}
		return noSkipped;
	}

	@Override
	public void close()
	{
		// This can safely be closed several times
		m_closed = true;
	}
}
