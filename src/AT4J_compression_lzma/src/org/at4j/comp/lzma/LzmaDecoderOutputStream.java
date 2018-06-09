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
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
final class LzmaDecoderOutputStream extends OutputStream
{
	private final LinkedBlockingQueue<byte[]> m_dataQueue;

	private boolean m_closed;
	private boolean m_interrupted;

	LzmaDecoderOutputStream(LinkedBlockingQueue<byte[]> q)
	{
		m_dataQueue = q;
	}

	private void assertNotClosed() throws IOException
	{
		if (m_closed)
		{
			throw new IOException("This stream is closed");
		}
	}

	private void put(byte[] barr) throws IOException
	{
		// Don't put anything here if the current thread is interrupted. That
		// may cause
		try
		{
			m_dataQueue.put(barr);
		}
		catch (InterruptedException e)
		{
			// Reset the interrupt flag and let the LZMA decoder react to that.
			m_interrupted = true;
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void write(int b) throws IOException
	{
		assertNotClosed();
		put(new byte[] { (byte) (b & 0xFF) });
	}

	@Override
	public void write(byte[] barr) throws IOException
	{
		assertNotClosed();
		// Write a defensive copy. The LZMA decoder reuses the byte array that
		// it uses when calling this method.
		byte[] barr2 = new byte[barr.length];
		System.arraycopy(barr, 0, barr2, 0, barr.length);
		put(barr2);
	}

	@Override
	public void write(byte[] barr, int off, int len) throws IOException
	{
		assertNotClosed();
		// Assume that the decoder gets the offset and length right. Don't check.

		// Write a defensive copy. The LZMA decoder reuses the byte array that
		// it uses when calling this method.
		byte[] barr2 = new byte[len];
		System.arraycopy(barr, off, barr2, 0, len);
		put(barr2);
	}

	@Override
	public void close() throws IOException
	{
		if (!m_closed)
		{
			try
			{
				if (!m_interrupted)
				{
					// Add an EOF block to the queue.
					try
					{
						m_dataQueue.put(new byte[0]);
					}
					catch (InterruptedException e)
					{
						// Reset the interrupt flag on the current thread.
						Thread.currentThread().interrupt();
					}
				}
				super.close();
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
