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
package org.at4j.support.comp;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class IntMoveToFrontTest
{
	@Test
	public void testEncodeWithAlphabet()
	{
		// This example is from Data Compression, The Complete Reference, Fourth
		// Edition, Section 8.1.1
		IntMoveToFront mtf = new IntMoveToFront(new int[] { ' ', 'i', 'm', 's', 'w' });
		assertTrue(Arrays.equals(new int[] { 3, 4, 4, 3, 3, 4, 0, 1, 0, 0 }, mtf.encode(new int[] { 's', 'w', 'm', ' ', 's', 'i', 'i', 's', 's', 's' }, new int[10])));

		// This example is from Data Compression, The Complete Reference, Fourth
		// Edition, Section 1.5
		mtf = new IntMoveToFront(new int[] { 'a', 'b', 'c', 'd', 'm', 'n', 'o', 'p' });
		assertTrue(Arrays.equals(new int[] { 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3 }, mtf.encode(new int[] { 'a', 'b', 'c', 'd', 'd', 'c', 'b', 'a', 'm', 'n', 'o', 'p', 'p', 'o', 'n', 'm' }, new int[16])));
	}

	@Test
	public void testEncodeWithRange()
	{
		// This example is from the Wikipedia page on Move To Front transformation
		IntMoveToFront mtf = new IntMoveToFront(0, 7);
		assertTrue(Arrays.equals(new int[] { 5, 3, 5, 7, 4, 0, 1, 5, 1 }, mtf.encode(new int[] { 5, 2, 4, 7, 0, 0, 7, 1, 7 }, new int[9])));
	}

	@Test
	public void testDecodeWithAlphabet()
	{
		// This example is from Data Compression, The Complete Reference, Fourth
		// Edition, Section 8.1.1
		IntMoveToFront mtf = new IntMoveToFront(new int[] { ' ', 'i', 'm', 's', 'w' });
		assertTrue(Arrays.equals(new int[] { 's', 'w', 'm', ' ', 's', 'i', 'i', 's', 's', 's' }, mtf.decode(new int[] { 3, 4, 4, 3, 3, 4, 0, 1, 0, 0 }, new int[10])));

		// This example is from Data Compression, The Complete Reference, Fourth
		// Edition, Section 1.5
		mtf = new IntMoveToFront(new int[] { 'a', 'b', 'c', 'd', 'm', 'n', 'o', 'p' });
		assertTrue(Arrays.equals(new int[] { 'a', 'b', 'c', 'd', 'd', 'c', 'b', 'a', 'm', 'n', 'o', 'p', 'p', 'o', 'n', 'm' }, mtf.decode(new int[] { 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3 }, new int[16])));
	}

	@Test
	public void testDecodeWithRange()
	{
		// This example is from the Wikipedia page on Move To Front transformation
		IntMoveToFront mtf = new IntMoveToFront(0, 7);
		assertTrue(Arrays.equals(new int[] { 5, 2, 4, 7, 0, 0, 7, 1, 7 }, mtf.decode(new int[] { 5, 3, 5, 7, 4, 0, 1, 5, 1 }, new int[9])));
	}
}
