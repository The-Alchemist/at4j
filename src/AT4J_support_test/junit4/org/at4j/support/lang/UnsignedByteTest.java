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
package org.at4j.support.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.at4j.support.lang.UnsignedByte;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnsignedByteTest
{
	@Test
	public void testValueOf()
	{
		try
		{
			UnsignedByte.valueOf((short) -1);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}

		try
		{
			UnsignedByte.valueOf((short) 256);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}

		assertEquals(0, UnsignedByte.valueOf((short) 0).shortValue());
		assertEquals(255, UnsignedByte.valueOf((short) 255).shortValue());
		assertEquals(-1, UnsignedByte.valueOf((short) 255).byteValue());

		assertEquals((short) 255, UnsignedByte.valueOf((byte) -1).shortValue());
	}

	@Test
	public void testIsBitSet()
	{
		UnsignedByte b = UnsignedByte.valueOf((short) 0);
		assertFalse(b.isBitSet(0));
		assertFalse(b.isBitSet(1));
		assertFalse(b.isBitSet(2));
		assertFalse(b.isBitSet(3));
		assertFalse(b.isBitSet(4));
		assertFalse(b.isBitSet(5));
		assertFalse(b.isBitSet(6));
		assertFalse(b.isBitSet(7));

		b = UnsignedByte.valueOf((short) 255);
		assertTrue(b.isBitSet(0));
		assertTrue(b.isBitSet(1));
		assertTrue(b.isBitSet(2));
		assertTrue(b.isBitSet(3));
		assertTrue(b.isBitSet(4));
		assertTrue(b.isBitSet(5));
		assertTrue(b.isBitSet(6));
		assertTrue(b.isBitSet(7));

		b = UnsignedByte.valueOf((short) (1 + 8 + 64));
		assertTrue(b.isBitSet(0));
		assertFalse(b.isBitSet(1));
		assertFalse(b.isBitSet(2));
		assertTrue(b.isBitSet(3));
		assertFalse(b.isBitSet(4));
		assertFalse(b.isBitSet(5));
		assertTrue(b.isBitSet(6));
		assertFalse(b.isBitSet(7));
	}

	@Test
	public void testCompareTo()
	{
		assertTrue(UnsignedByte.ZERO.compareTo(UnsignedByte.ZERO) == 0);
		assertTrue(UnsignedByte.ONE.compareTo(UnsignedByte.ONE) == 0);
		assertTrue(UnsignedByte.ZERO.compareTo(UnsignedByte.ONE) < 0);
		assertTrue(UnsignedByte.ONE.compareTo(UnsignedByte.ZERO) > 0);
		assertTrue(UnsignedByte.valueOf(UnsignedByte.MAX_VALUE).compareTo(UnsignedByte.ZERO) > 0);
		assertTrue(UnsignedByte.ZERO.compareTo(UnsignedByte.valueOf(UnsignedByte.MAX_VALUE)) < 0);
		assertTrue(UnsignedByte.valueOf(12).compareTo(UnsignedByte.valueOf(53)) < 0);
	}
}
