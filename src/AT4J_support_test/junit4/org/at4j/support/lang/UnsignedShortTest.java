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

import org.at4j.support.lang.UnsignedShort;
import org.entityfs.exception.UnexpectedEofException;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnsignedShortTest
{
	@Test
	public void testValueOf()
	{
		assertEquals(UnsignedShort.MAX_VALUE, UnsignedShort.valueOf((short) -1).intValue());

		try
		{
			UnsignedShort.valueOf(65536);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// ok
		}

		assertEquals(0, UnsignedShort.valueOf(0).intValue());
		assertEquals(65535, UnsignedShort.valueOf(65535).intValue());
	}

	@Test
	public void testGetBigEndianBytes()
	{
		assertTrue(Arrays.equals(new byte[] { 0x2B, 0x1A }, UnsignedShort.valueOf(0x1A2B).getBigEndianByteArray()));
	}

	@Test
	public void testFromBigEndianByteArray()
	{
		assertTrue(Arrays.equals(new byte[] { 0x1A, 0x2B }, UnsignedShort.fromBigEndianByteArray(new byte[] { 0x1A, 0x2B }).getBigEndianByteArray()));
		try
		{
			UnsignedShort.fromBigEndianByteArray(new byte[3]);
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
		byte[] barr = new byte[] { 0x1A, 0x2B, 0x3C };
		ByteArrayInputStream is = new ByteArrayInputStream(barr);
		try
		{
			assertTrue(Arrays.equals(new byte[] { 0x1A, 0x2B }, UnsignedShort.readBigEndian(is).getBigEndianByteArray()));
		}
		finally
		{
			is.close();
		}

		barr = new byte[1];
		is = new ByteArrayInputStream(barr);
		try
		{
			UnsignedShort.readBigEndian(is);
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
		assertTrue(UnsignedShort.ZERO.compareTo(UnsignedShort.ZERO) == 0);
		assertTrue(UnsignedShort.ONE.compareTo(UnsignedShort.ONE) == 0);
		assertTrue(UnsignedShort.ZERO.compareTo(UnsignedShort.ONE) < 0);
		assertTrue(UnsignedShort.ONE.compareTo(UnsignedShort.ZERO) > 0);
		assertTrue(UnsignedShort.valueOf(UnsignedShort.MAX_VALUE).compareTo(UnsignedShort.ZERO) > 0);
		assertTrue(UnsignedShort.ZERO.compareTo(UnsignedShort.valueOf(UnsignedShort.MAX_VALUE)) < 0);
		assertTrue(UnsignedShort.valueOf(12).compareTo(UnsignedShort.valueOf(53)) < 0);
	}
}
