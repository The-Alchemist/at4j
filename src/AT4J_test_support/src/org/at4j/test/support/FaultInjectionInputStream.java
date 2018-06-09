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
package org.at4j.test.support;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an input stream that faults can be injected into.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class FaultInjectionInputStream extends FilterInputStream
{
	private volatile boolean m_fault;

	public FaultInjectionInputStream(InputStream is)
	{
		super(is);
	}

	public void injectFault()
	{
		m_fault = true;
	}

	private void testAndClearFault() throws IOException
	{
		if (m_fault)
		{
			m_fault = false;
			throw new IOException("Injected fault");
		}
	}

	@Override
	public int read() throws IOException
	{
		testAndClearFault();
		return super.read();
	}

	@Override
	public int read(byte[] barr) throws IOException
	{
		testAndClearFault();
		return super.read(barr);
	}

	@Override
	public int read(byte[] barr, int off, int len) throws IOException
	{
		testAndClearFault();
		return super.read(barr, off, len);
	}

	@Override
	public int available() throws IOException
	{
		testAndClearFault();
		return super.available();
	}

	@Override
	public long skip(long n) throws IOException
	{
		testAndClearFault();
		return super.skip(n);
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			testAndClearFault();
		}
		finally
		{
			super.close();
		}
	}
}
