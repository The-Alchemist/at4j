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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.at4j.support.lang.UnsignedLong;
import org.entityfs.exception.UnexpectedEofException;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnsignedLongTest
{
	@Test
	public void testValueOf()
	{
		assertEquals(0L, UnsignedLong.valueOf(0L).bigIntValue().longValue());
		assertEquals(Long.MAX_VALUE, UnsignedLong.valueOf(Long.MAX_VALUE).bigIntValue().longValue());

		BigInteger b = BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE);
		assertEquals(b, UnsignedLong.valueOf(b).bigIntValue());

		try
		{
			UnsignedLong.valueOf(b.add(BigInteger.ONE));
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}

		assertEquals(UnsignedLong.MAX_VALUE, UnsignedLong.valueOf(-1L).bigIntValue());

		try
		{
			UnsignedLong.valueOf(BigInteger.ZERO.subtract(BigInteger.ONE));
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}
	}

	@Test
	public void testGetBigEndianBytes()
	{
		assertTrue(Arrays.equals(new byte[] { 0x01, 0x70, 0x6F, 0x5E, 0x4D, 0x3C, 0x2B, 0x1A }, UnsignedLong.valueOf(0x1A2B3C4D5E6F7001L).getBigEndianByteArray()));
	}

	@Test
	public void testFromBigEndianByteArray()
	{
		assertTrue(Arrays.equals(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x01 }, UnsignedLong.fromBigEndianByteArray(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x01 }).getBigEndianByteArray()));
		try
		{
			UnsignedLong.fromBigEndianByteArray(new byte[3]);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}
	}

	@Test
	public void testReadBigEndian() throws IOException
	{
		byte[] barr = new byte[] { 0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x01, 0x12 };
		ByteArrayInputStream is = new ByteArrayInputStream(barr);
		try
		{
			assertTrue(Arrays.equals(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x01 }, UnsignedLong.readBigEndian(is).getBigEndianByteArray()));
		}
		finally
		{
			is.close();
		}

		barr = new byte[3];
		is = new ByteArrayInputStream(barr);
		try
		{
			UnsignedLong.readBigEndian(is);
			fail();
		}
		catch (UnexpectedEofException e)
		{
			// ok
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testCompareTo()
	{
		assertTrue(UnsignedLong.ZERO.compareTo(UnsignedLong.ZERO) == 0);
		assertTrue(UnsignedLong.ONE.compareTo(UnsignedLong.ONE) == 0);
		assertTrue(UnsignedLong.ZERO.compareTo(UnsignedLong.ONE) < 0);
		assertTrue(UnsignedLong.ONE.compareTo(UnsignedLong.ZERO) > 0);
		assertTrue(UnsignedLong.valueOf(UnsignedLong.MAX_VALUE).compareTo(UnsignedLong.ZERO) > 0);
		assertTrue(UnsignedLong.ZERO.compareTo(UnsignedLong.valueOf(UnsignedLong.MAX_VALUE)) < 0);
		assertTrue(UnsignedLong.valueOf(12).compareTo(UnsignedLong.valueOf(53)) < 0);
	}
}
