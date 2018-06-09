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
import java.util.Arrays;

import org.entityfs.exception.UnexpectedEofException;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnsignedIntegerTest
{
	@Test
	public void testValueOf()
	{
		long maxValue = ((long) Integer.MAX_VALUE + 1) * 2 - 1;
		try
		{
			UnsignedInteger.valueOf(-1L);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}

		try
		{
			UnsignedInteger.valueOf(maxValue + 1);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}

		assertEquals(0L, UnsignedInteger.valueOf(0L).longValue());
		assertEquals(maxValue, UnsignedInteger.valueOf(maxValue).longValue());
	}

	@Test
	public void testGetBigEndianBytes()
	{
		assertTrue(Arrays.equals(new byte[] { 0x4D, 0x3C, 0x2B, 0x1A }, UnsignedInteger.valueOf(0x1A2B3C4D).getBigEndianByteArray()));
	}

	@Test
	public void testFromBigEndianByteArray()
	{
		assertTrue(Arrays.equals(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D }, UnsignedInteger.fromBigEndianByteArray(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D }).getBigEndianByteArray()));
		try
		{
			UnsignedInteger.fromBigEndianByteArray(new byte[3]);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}
	}

	@Test
	public void testFromBigEndianByteArrayToLong()
	{
		assertEquals(0x1A2B3C4DL, UnsignedInteger.fromBigEndianByteArrayToLong(new byte[] { 0x4D, 0x3C, 0x2B, 0x1A }, 0));
	}

	@Test
	public void testFromLittleEndianByteArrayToLong()
	{
		assertEquals(0x1A2B3C4DL, UnsignedInteger.fromLittleEndianByteArrayToLong(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D }, 0));
	}

	@Test
	public void testReadBigEndian() throws IOException
	{
		byte[] barr = new byte[] { 0x1A, 0x2B, 0x3C, 0x4D, 0x5E };
		ByteArrayInputStream is = new ByteArrayInputStream(barr);
		try
		{
			assertTrue(Arrays.equals(new byte[] { 0x1A, 0x2B, 0x3C, 0x4D }, UnsignedInteger.readBigEndian(is).getBigEndianByteArray()));
		}
		finally
		{
			is.close();
		}

		barr = new byte[3];
		is = new ByteArrayInputStream(barr);
		try
		{
			UnsignedInteger.readBigEndian(is);
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
		assertTrue(UnsignedInteger.ZERO.compareTo(UnsignedInteger.ZERO) == 0);
		assertTrue(UnsignedInteger.ONE.compareTo(UnsignedInteger.ONE) == 0);
		assertTrue(UnsignedInteger.ZERO.compareTo(UnsignedInteger.ONE) < 0);
		assertTrue(UnsignedInteger.ONE.compareTo(UnsignedInteger.ZERO) > 0);
		assertTrue(UnsignedInteger.valueOf(UnsignedInteger.MAX_VALUE).compareTo(UnsignedInteger.ZERO) > 0);
		assertTrue(UnsignedInteger.ZERO.compareTo(UnsignedInteger.valueOf(UnsignedInteger.MAX_VALUE)) < 0);
		assertTrue(UnsignedInteger.valueOf(12).compareTo(UnsignedInteger.valueOf(53)) < 0);
	}
}
