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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.entityfs.support.io.StreamUtil;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BurrowsWheelerDecoderTest extends AbstractBurrowsWheelerTest
{
	@Test
	public void testDecode1() throws IOException
	{
		// Example from the Wikipedia article on Burrows Wheeler
		byte[] data = createData("BNN^AAxA");
		byte[] readData = StreamUtil.readStreamFully(new BurrowsWheelerDecoder(data, data.length, calcByteFrequencies(data), 6).decode(), 32);
		assertTrue(Arrays.equals(createData("^BANANAx"), readData));
	}

	@Test
	public void testDecode2() throws IOException
	{
		// Example from Data Compression, The Complete Reference, Fourth Edition
		byte[] data = createData("swm siisss");
		byte[] readData = StreamUtil.readStreamFully(new BurrowsWheelerDecoder(data, data.length, calcByteFrequencies(data), 8).decode(), 32);
		assertTrue(Arrays.equals(createData("swiss miss"), readData));
	}

	// More tests in BurrowsWheelerEncoderTest
}
