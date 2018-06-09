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

import org.at4j.support.lang.UnsignedLong;

import SevenZip.Compression.LZMA.Decoder;

/**
 * This is run in its own thread by the {@link LzmaDecoderOutputStream}. It
 * @author Karl Gustafsson
 * @since 1.0
 */
final class LzmaReaderRunnable implements Runnable
{
	private static final int STREAM_PROPERTIES_SIZE = 5; // bytes

	private final LzmaErrorState m_errorState;
	private final LzmaDecoderOutputStream m_queueStream;
	private final InputStream m_inStream;
	private final LzmaInputStreamSettings m_settings;

	LzmaReaderRunnable(InputStream inStream, LzmaErrorState errState, LzmaDecoderOutputStream queueStream, LzmaInputStreamSettings settings)
	{
		m_inStream = inStream;
		m_errorState = errState;
		m_queueStream = queueStream;
		m_settings = settings;
	}

	public void run()
	{
		try
		{
			try
			{
				byte[] properties;
				if (m_settings.getProperties() != null)
				{
					// Use the supplied properties
					properties = m_settings.getProperties();
				}
				else
				{
					// Read the properties from the stream
					properties = new byte[STREAM_PROPERTIES_SIZE];

					int noRead = m_inStream.read(properties);
					if (noRead != STREAM_PROPERTIES_SIZE)
					{
						throw new IOException("Wanted to read " + STREAM_PROPERTIES_SIZE + " bytes. Got " + noRead);
					}
				}

				long size = m_settings.isReadUncompressedSize() ? UnsignedLong.readBigEndian(m_inStream).longValue() : -1;

				// Either one, or both of these may be set to -1 (size unknown).
				// If we have a non-negative value, use it!
				size = Math.max(size, m_settings.getUncompressedSize());

				Decoder dec = new Decoder();
				if (!dec.SetDecoderProperties(properties, size))
				{
					throw new IOException("Could not set LZMA decoder properties from the data in the supplied stream. No reason given.");
				}

				if (!dec.Code(m_inStream, m_queueStream, size))
				{
					throw new IOException("Could not decode stream. No reason given.");
				}
			}
			finally
			{
				m_queueStream.close();
			}
		}
		catch (IOException e)
		{
			m_errorState.setIoException(e);
		}
		catch (InterruptedException e)
		{
			// Ignore interruption errors. They are probably caused by the
			// owning thread closing its input stream before reaching EOF.
		}
		catch (RuntimeException e)
		{
			m_errorState.setRuntimeException(e);
		}
		catch (Error e)
		{
			m_errorState.setError(e);
		}
	}
}
