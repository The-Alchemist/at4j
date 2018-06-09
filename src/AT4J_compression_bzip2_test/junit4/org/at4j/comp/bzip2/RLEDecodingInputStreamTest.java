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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class RLEDecodingInputStreamTest
{
	@Test
	public void testRead() throws IOException
	{
		byte[] in = new byte[] { 1, 1, 1, 1, 4, 2, 2, 2, 2 };
		byte[] out = new byte[12];
		RLEDecodingInputStream is = new RLEDecodingInputStream(new ByteArrayInputStream(in), 715247535);
		try
		{
			is.read(out);
			assertTrue(Arrays.equals(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2 }, out));
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}
	}
}
