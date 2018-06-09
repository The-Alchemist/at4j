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

import org.at4j.support.lang.SignedLong;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class SignedLongTest
{
	@Test
	public void testBigEndianByteArray()
	{
		assertEquals(Long.MAX_VALUE, SignedLong.fromBigEndianByteArray(SignedLong.valueOf(Long.MAX_VALUE).getBigEndianByteArray()).longValue());
		assertEquals(Long.MIN_VALUE, SignedLong.fromBigEndianByteArray(SignedLong.valueOf(Long.MIN_VALUE).getBigEndianByteArray()).longValue());
		assertEquals(0L, SignedLong.fromBigEndianByteArray(SignedLong.valueOf(0L).getBigEndianByteArray()).longValue());
		assertEquals(1L, SignedLong.fromBigEndianByteArray(SignedLong.valueOf(1L).getBigEndianByteArray()).longValue());
		assertEquals(-1L, SignedLong.fromBigEndianByteArray(SignedLong.valueOf(-1L).getBigEndianByteArray()).longValue());
		assertEquals(-13L, SignedLong.fromBigEndianByteArray(SignedLong.valueOf(-13).getBigEndianByteArray()).longValue());
	}

	@Test
	public void testLittleEndianByteArray()
	{
		assertEquals(Long.MAX_VALUE, SignedLong.fromLittleEndianByteArray(SignedLong.valueOf(Long.MAX_VALUE).getLittleEndianByteArray()).longValue());
		assertEquals(Long.MIN_VALUE, SignedLong.fromLittleEndianByteArray(SignedLong.valueOf(Long.MIN_VALUE).getLittleEndianByteArray()).longValue());
		assertEquals(0L, SignedLong.fromLittleEndianByteArray(SignedLong.valueOf(0L).getLittleEndianByteArray()).longValue());
		assertEquals(1L, SignedLong.fromLittleEndianByteArray(SignedLong.valueOf(1L).getLittleEndianByteArray()).longValue());
		assertEquals(-1L, SignedLong.fromLittleEndianByteArray(SignedLong.valueOf(-1L).getLittleEndianByteArray()).longValue());
		assertEquals(-13L, SignedLong.fromLittleEndianByteArray(SignedLong.valueOf(-13).getLittleEndianByteArray()).longValue());
	}
}
