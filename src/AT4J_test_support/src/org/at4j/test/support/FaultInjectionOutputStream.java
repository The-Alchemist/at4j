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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an output stream that faults can be injected into.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class FaultInjectionOutputStream extends FilterOutputStream
{
	private volatile boolean m_fault;
	private volatile boolean m_hasThrown;

	public FaultInjectionOutputStream(OutputStream os)
	{
		super(os);
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
			m_hasThrown = true;
			throw new IOException("Injected fault");
		}
	}
	
	public boolean testAndClearHasThrown()
	{
		boolean res = m_hasThrown;
		m_hasThrown = false;
		return res;
	}

	@Override
	public void write(int b) throws IOException
	{
		testAndClearFault();
		super.write(b);
	}

	@Override
	public void write(byte[] barr) throws IOException
	{
		testAndClearFault();
		super.write(barr);
	}

	@Override
	public void write(byte[] barr, int off, int len) throws IOException
	{
		testAndClearFault();
		super.write(barr, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		testAndClearFault();
		super.flush();
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
