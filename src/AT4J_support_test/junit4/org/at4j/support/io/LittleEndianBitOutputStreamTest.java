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
package org.at4j.support.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class LittleEndianBitOutputStreamTest
{
	@Test
	public void testWrite() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			os.write(12);
			os.write(120);

			os.writeBit(true);
			try
			{
				os.write(12);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("boundary"));
			}

			assertEquals(1, os.getNumberOfBitsInUnfinishedByte());
			assertEquals(1, os.getUnfinishedByte());
		}
		finally
		{
			os.close();
		}

		assertTrue(Arrays.equals(new byte[] { 12, 120 }, baos.toByteArray()));

		baos = new ByteArrayOutputStream();
		os = new LittleEndianBitOutputStream(baos);
		try
		{
			os.write(new byte[] { 1, 2, 12, 23 }, 1, 2);

			os.writeBit(true);
			try
			{
				os.write(new byte[] { 1 });
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("boundary"));
			}
		}
		finally
		{
			os.close();
		}

		assertTrue(Arrays.equals(new byte[] { 2, 12 }, baos.toByteArray()));
	}

	@Test
	public void testWriteBit() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			// 10101010|01010101
			os.writeBit(true);
			os.writeBit(false);
			os.writeBit(true);
			os.writeBit(false);
			os.writeBit(true);
			os.writeBit(false);
			os.writeBit(true);
			os.writeBit(false);
			os.writeBit(false);
			os.writeBit(true);
			os.writeBit(false);
			os.writeBit(true);
			os.writeBit(false);
			os.writeBit(true);

			assertEquals(6, os.getNumberOfBitsInUnfinishedByte());
			assertEquals(16 + 4 + 1, os.getUnfinishedByte());

			os.writeBit(false);
			os.writeBit(true);
		}
		finally
		{
			os.close();
		}

		byte[] out = baos.toByteArray();
		assertTrue(Arrays.equals(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), 64 + 16 + 4 + 1 }, out));
	}

	@Test
	public void testWriteBits() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			os.writeBits(1 + 4, 3);
			os.writeBits(2, 3);
			os.writeBits(1 + 4, 3);
			os.writeBits(2, 3);
			os.writeBits(1 + 4, 3);
			os.writeBits(2, 3);
			os.writeBits(1 + 4, 3);
			os.writeBits(2, 3);
			os.write(1);
		}
		finally
		{
			os.close();
		}

		byte[] out = baos.toByteArray();
		byte b = (byte) ((128 + 32 + 8 + 2) & 0xFF);
		assertTrue(Arrays.equals(new byte[] { b, b, b, 1 }, out));
	}

	@Test
	public void testPadToByteBoundary() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			// 01000000
			os.writeBit(false);
			os.writeBit(true);

			assertEquals(2, os.getNumberOfBitsInUnfinishedByte());
			assertEquals(1, os.getUnfinishedByte());

			os.padToByteBoundary();
			// This should do nothing
			os.padToByteBoundary();
		}
		finally
		{
			os.close();
		}

		byte[] out = baos.toByteArray();
		assertTrue(Arrays.equals(new byte[] { 64 }, out));
	}

	@Test
	public void testWriteBitsLittleEndian1() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			// 101
			os.writeBitsLittleEndian(4 + 1, 3);
			// 01010
			os.writeBitsLittleEndian(8 + 2, 5);
			// 10101010
			os.writeBitsLittleEndian(128 + 32 + 8 + 2, 8);
			// 1010101|01010101
			os.writeBitsLittleEndian(16384 + 4096 + 1024 + 256 + 64 + 16 + 4 + 1, 15);
			// 01010101|01010101
			os.writeBitsLittleEndian(16384 + 4096 + 1024 + 256 + 64 + 16 + 4 + 1, 16);
			// 010101|01010101
			os.writeBitsLittleEndian(4096 + 1024 + 256 + 64 + 16 + 4 + 1, 14);
			// 01|01010101|01010101
			os.writeBitsLittleEndian(65536 + 16384 + 4096 + 1024 + 256 + 64 + 16 + 4 + 1, 18);
			os.writeBit(false);
		}
		finally
		{
			os.close();
		}

		byte[] out = baos.toByteArray();
		byte b = -128 + 32 + 8 + 2;
		assertTrue(Arrays.equals(new byte[] { b, b, b, b, b, b, b, b, b, b }, out));
	}

	@Test
	public void testWriteBitsLittleEndian2() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			// 00111111|10100000
			os.writeBits(0, 2);
			os.writeBitsLittleEndian(509, 9);
			os.padToByteBoundary();
		}
		finally
		{
			os.close();
		}
		assertTrue(Arrays.equals(baos.toByteArray(), new byte[] { 63, -96 }));
	}

	@Test
	public void testWriteBitsLittleEndian3() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			// 01111111|00111111|10100000
			os.writeBits(0, 1);
			os.writeBitsLittleEndian(508, 9);
			os.writeBitsLittleEndian(509, 9);
			os.padToByteBoundary();
		}
		finally
		{
			os.close();
		}
		assertTrue(Arrays.equals(baos.toByteArray(), new byte[] { 127, 63, -96 }));
	}

	@Test
	public void testWriteBytes() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream os = new LittleEndianBitOutputStream(baos);
		try
		{
			// 10101010
			os.writeBytes(new byte[] { 0, -128 + 32 + 8 + 2, 1 }, 1, 1);
			// 1
			os.writeBit(true);
			// 01010101|01010101
			os.writeBytes(new byte[] { 1, 64 + 16 + 4 + 1, 64 + 16 + 4 + 1, 1 }, 1, 2);
			// 0
			os.writeBit(false);
			// 10101010|10101010
			os.writeBytes(new byte[] { -128 + 32 + 8 + 2, -128 + 32 + 8 + 2 }, 0, 2);
			// 101010
			os.writeBits(32 + 8 + 2, 6);
		}
		finally
		{
			os.close();
		}

		byte[] out = baos.toByteArray();
		byte b = -128 + 32 + 8 + 2;
		assertTrue(Arrays.equals(new byte[] { b, b, b, b, b, b }, out));
	}
}
