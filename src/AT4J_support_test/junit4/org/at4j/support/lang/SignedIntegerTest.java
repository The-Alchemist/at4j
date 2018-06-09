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

import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class SignedIntegerTest
{
	@Test
	public void testBigEndianByteArray()
	{
		assertEquals(Integer.MAX_VALUE, SignedInteger.fromBigEndianByteArray(SignedInteger.valueOf(Integer.MAX_VALUE).getBigEndianByteArray()).intValue());
		assertEquals(Integer.MIN_VALUE, SignedInteger.fromBigEndianByteArray(SignedInteger.valueOf(Integer.MIN_VALUE).getBigEndianByteArray()).intValue());
		assertEquals(0L, SignedInteger.fromBigEndianByteArray(SignedInteger.valueOf(0).getBigEndianByteArray()).intValue());
		assertEquals(1L, SignedInteger.fromBigEndianByteArray(SignedInteger.valueOf(1).getBigEndianByteArray()).intValue());
		assertEquals(-1L, SignedInteger.fromBigEndianByteArray(SignedInteger.valueOf(-1).getBigEndianByteArray()).intValue());
		assertEquals(-13L, SignedInteger.fromBigEndianByteArray(SignedInteger.valueOf(-13).getBigEndianByteArray()).intValue());
	}

	@Test
	public void testLittleEndianByteArray()
	{
		assertEquals(Integer.MAX_VALUE, SignedInteger.fromLittleEndianByteArray(SignedInteger.valueOf(Integer.MAX_VALUE).getLittleEndianByteArray()).intValue());
		assertEquals(Integer.MIN_VALUE, SignedInteger.fromLittleEndianByteArray(SignedInteger.valueOf(Integer.MIN_VALUE).getLittleEndianByteArray()).intValue());
		assertEquals(0L, SignedInteger.fromLittleEndianByteArray(SignedInteger.valueOf(0).getLittleEndianByteArray()).intValue());
		assertEquals(1L, SignedInteger.fromLittleEndianByteArray(SignedInteger.valueOf(1).getLittleEndianByteArray()).intValue());
		assertEquals(-1L, SignedInteger.fromLittleEndianByteArray(SignedInteger.valueOf(-1).getLittleEndianByteArray()).intValue());
		assertEquals(-13L, SignedInteger.fromLittleEndianByteArray(SignedInteger.valueOf(-13).getLittleEndianByteArray()).intValue());
	}
}
