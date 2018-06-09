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
package org.at4j.tar.builder;

import java.io.IOException;
import java.io.InputStream;

import org.entityfs.ReadWritableFile;
import org.entityfs.el.AbsoluteLocation;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class TarBuilderTest extends AbstractTarBuilderTest<TarBuilder>
{
	@Override
	protected TarBuilder createTarBuilder(ReadWritableFile raf, TarBuilderSettings settings)
	{
		return new TarBuilder(raf, settings);
	}

	@Override
	protected void addDataFromStream(TarBuilder tb, InputStream is, AbsoluteLocation l, TarEntrySettings settings)
	{
		try
		{
			try
			{
				if (settings != null)
				{
					tb.add(is, l, settings);
				}
				else
				{
					tb.add(is, l);
				}
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	// So that this is recognized as a test class
	@Test
	public void testDummy()
	{
		// Empty
	}
}
