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

/**
 * This class is used for propagating errors up from the LZMA encoder to the
 * client thread that writes data to it.
 * <p>
 * This object is thread safe.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class LzmaErrorState
{
	// The different kinds of errors that can occur in the encoder.
	private Error m_error;
	private RuntimeException m_runtimeException;
	private IOException m_ioException;

	synchronized void setError(Error e)
	{
		m_error = e;
	}

	synchronized void setRuntimeException(RuntimeException e)
	{
		m_runtimeException = e;
	}

	synchronized void setIoException(IOException e)
	{
		m_ioException = e;
	}

	synchronized void testAndClearErrors() throws IOException, RuntimeException, Error
	{
		if (m_error != null)
		{
			Error e = m_error;
			m_error = null;
			throw e;
		}
		else if (m_runtimeException != null)
		{
			RuntimeException e = m_runtimeException;
			m_runtimeException = e;
			throw e;
		}
		else if (m_ioException != null)
		{
			IOException e = m_ioException;
			m_ioException = null;
			throw e;
		}
	}
}
