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

import org.at4j.support.lang.SignedLong;

import SevenZip.Compression.LZMA.Encoder;

/**
 * This thread is used by the {@link LzmaOutputStream} to write to the LZMA
 * encoder. It shares some state variables with the client that starts this
 * thread:
 * <ul>
 * <li>A {@link LzmaEncoderInputStream} that the {@link LzmaOutputStream} writes
 * data to via a blocking queue and the encoder reads data from.</li>
 * <li>An {@link ErrorState} object to propagate errors up from the encoder</li>
 * <li>An output stream to write to</li>
 * </ul>
 * @author Karl Gustafsson
 * @since 1.0
 */
final class LzmaWriterRunnable implements Runnable
{
	private final LzmaErrorState m_errorState;
	private final Encoder m_encoder;
	private final LzmaOutputStreamSettings m_settings;
	private final long m_uncompressedDataSize;
	private final LzmaEncoderInputStream m_queueStream;
	private final OutputStream m_outStream;

	LzmaWriterRunnable(LzmaEncoderInputStream writerStream, Encoder enc, LzmaOutputStreamSettings settings, long uncompressedDataSize, LzmaErrorState errState, OutputStream outStream)
	{
		m_errorState = errState;
		m_encoder = enc;
		m_settings = settings;
		m_uncompressedDataSize = uncompressedDataSize;
		m_queueStream = writerStream;
		m_outStream = outStream;
	}

	public void run()
	{
		try
		{
			if (m_settings.isWriteStreamProperties())
			{
				m_encoder.WriteCoderProperties(m_outStream);
			}

			if (m_settings.isWriteUncompressedDataSize())
			{
				m_outStream.write(SignedLong.valueOf(m_uncompressedDataSize).getBigEndianByteArray());
			}

			// inSize and outSize variables are not used by the encoder. They
			// are set to -1 here.
			// Set the progress meter to null
			m_encoder.Code(m_queueStream, m_outStream, -1, -1, null);
		}
		catch (Error e)
		{
			m_errorState.setError(e);
		}
		catch (RuntimeException e)
		{
			m_errorState.setRuntimeException(e);
		}
		catch (IOException e)
		{
			m_errorState.setIoException(e);
		}
	}
}
