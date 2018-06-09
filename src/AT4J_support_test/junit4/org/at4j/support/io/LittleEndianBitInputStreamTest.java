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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class LittleEndianBitInputStreamTest
{
	@Test
	public void testReadBit() throws IOException
	{
		// 10101010
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF) }));
		try
		{
			assertFalse(in.isAtEof());

			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.isAtEof());
			assertFalse(in.readBit());
			assertTrue(in.isAtEof());

			try
			{
				in.readBit();
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
		}
		finally
		{
			in.close();
		}

		// 01010101
		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) (64 + 16 + 4 + 1) }));
		try
		{
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());

			try
			{
				in.readBit();
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}

		// 01010101|10101010
		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) (64 + 16 + 4 + 1), (byte) ((128 + 32 + 8 + 2) & 0xFF) }));
		try
		{
			assertFalse(in.isAtEof());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.isAtEof());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			assertTrue(in.readBit());
			assertFalse(in.isAtEof());
			assertFalse(in.readBit());
			assertTrue(in.isAtEof());

			try
			{
				in.readBit();
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testRead() throws IOException
	{
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) 122 }));
		try
		{
			assertEquals(122, in.read());
			assertEquals(-1, in.read());

			in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) 122 }));
			in.readBit();
			try
			{
				in.read();
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("boundary"));
			}
		}
		finally
		{
			in.close();
		}

		byte[] barr2 = new byte[] { (byte) 1, (byte) 2, (byte) 4, (byte) 75 };
		in = new LittleEndianBitInputStream(new ByteArrayInputStream(barr2));
		try
		{
			byte[] barr = new byte[2];
			assertEquals(2, in.read(barr));
			assertTrue(Arrays.equals(barr, new byte[] { (byte) 1, (byte) 2 }));
			in.readBit();
			try
			{
				in.read(barr);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("boundary"));
				assertTrue(Arrays.equals(barr, new byte[] { (byte) 1, (byte) 2 }));
			}
			assertFalse(in.isAtEof());
			in.skipToByteBoundary();
			assertFalse(in.isAtEof());
			assertEquals(1, in.read(barr));
			assertTrue(Arrays.equals(barr, new byte[] { (byte) 75, (byte) 2 }));
			assertTrue(in.isAtEof());
			assertEquals(-1, in.read(barr));
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(barr2));
		try
		{
			byte[] barr = new byte[3];
			assertEquals(2, in.read(barr, 0, 2));
			assertTrue(Arrays.equals(barr, new byte[] { (byte) 1, (byte) 2, (byte) 0 }));
			in.readBit();
			try
			{
				in.read(barr, 0, 2);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("boundary"));
				assertTrue(Arrays.equals(barr, new byte[] { (byte) 1, (byte) 2, (byte) 0 }));
			}
			assertFalse(in.isAtEof());
			in.skipToByteBoundary();
			assertFalse(in.isAtEof());
			assertEquals(1, in.read(barr, 2, 1));
			assertTrue(Arrays.equals(barr, new byte[] { (byte) 1, (byte) 2, (byte) 75 }));
			assertTrue(in.isAtEof());
			assertEquals(-1, in.read(barr, 0, 3));
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testSkipToByteBoundary() throws IOException
	{
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) 122, (byte) 1, (byte) 4 }));
		try
		{
			in.skipToByteBoundary();
			assertEquals(122, in.read());
			in.readBit();
			in.skipToByteBoundary();
			assertEquals(4, in.read());
			try
			{
				in.skipToByteBoundary();
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testSkip() throws IOException
	{
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) 122, (byte) 1, (byte) 4, (byte) 75, (byte) 12 }));
		try
		{
			assertEquals(0L, in.skip(0L));
			in.readBit();
			try
			{
				in.skip(1L);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("boundary"));
			}
			in.skipToByteBoundary();

			assertEquals(2L, in.skip(2L));
			assertEquals(1L, in.skip(1L));
			assertEquals(1L, in.skip(2L));
			assertTrue(in.isAtEof());
			assertEquals(0L, in.skip(2L));
			assertEquals(0L, in.skip(1L));
			assertEquals(0L, in.skip(0L));
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testReadBytes() throws IOException
	{
		// Bytes aligned with the bytes in the stream
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) 122, (byte) 1, (byte) -4 }));
		try
		{
			byte[] barr = new byte[3];
			in.readBytes(barr, 1, 2);
			assertTrue(Arrays.equals(new byte[] { (byte) 0, (byte) 122, (byte) 1 }, barr));
			assertFalse(in.isAtEof());
			in.readBytes(barr, 0, 1);
			assertTrue(Arrays.equals(new byte[] { (byte) -4, (byte) 122, (byte) 1 }, barr));
			assertTrue(in.isAtEof());

			try
			{
				in.readBytes(barr, 0, 1);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { -126, -18 }));
		try
		{
			in.readBit();
			in.readBit();
			in.readBit();
			assertEquals(23, in.readBytes(new byte[1], 0, 1)[0]);
		}
		finally
		{
			in.close();
		}

		// Bytes that are not aligned with the bytes in the stream
		// 10101010|01010101
		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF), 0 }));
		try
		{
			assertTrue(in.readBit());
			assertFalse(in.readBit());
			byte[] barr = new byte[2];
			in.readBytes(barr, 0, 2);
			assertTrue(Arrays.equals(barr, new byte[] { (byte) ((128 + 32 + 8 + 1) & 0xFF), (byte) ((64 + 16 + 4) & 0xFF) }));
			assertFalse(in.readBit());
			assertFalse(in.readBit());
			assertFalse(in.readBit());
			assertFalse(in.readBit());
			assertFalse(in.readBit());
			assertFalse(in.isAtEof());
			assertFalse(in.readBit());
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testReadBits() throws IOException
	{
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			try
			{
				in.readBits(-1);
				fail();
			}
			catch (IndexOutOfBoundsException e)
			{
				// ok
			}

			try
			{
				in.readBits(9);
				fail();
			}
			catch (IndexOutOfBoundsException e)
			{
				// ok
			}
			assertEquals(0, in.readBits(0));
			// Bytes are stored little bit endian
			assertEquals(16 + 4 + 1, in.readBits(5));
			assertEquals(8 + 1, in.readBits(5));
			assertEquals(8 + 2, in.readBits(5));
			assertFalse(in.isAtEof());
			try
			{
				in.readBits(5);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			assertEquals(128 + 32 + 8 + 2, in.readBits(8));
			assertEquals(64 + 16 + 4 + 1, in.readBits(8));
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			assertEquals(64 + 16 + 4 + 1, in.readBits(7));
			assertEquals(16 + 4 + 1, in.readBits(7));
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			assertEquals(8 + 2, in.readBits(4));
			assertEquals(8 + 2, in.readBits(4));
			assertEquals(4 + 1, in.readBits(4));
			assertEquals(4 + 1, in.readBits(4));
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testReadBitsLittleEndian() throws IOException
	{
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			try
			{
				in.readBitsLittleEndian(-1);
				fail();
			}
			catch (IndexOutOfBoundsException e)
			{
				// ok
			}

			try
			{
				in.readBitsLittleEndian(33);
				fail();
			}
			catch (IndexOutOfBoundsException e)
			{
				// ok
			}
			assertEquals(0, in.readBits(0));

			// Bytes are stored little bit endian
			assertEquals(16 + 4 + 1, in.readBitsLittleEndian(5));
			assertEquals(256 + 32 + 8 + 2, in.readBitsLittleEndian(10));
			assertFalse(in.isAtEof());
			try
			{
				in.readBitsLittleEndian(5);
				fail();
			}
			catch (IOException e)
			{
				assertTrue(e.getMessage().contains("EOF"));
			}
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF), 1 }));
		try
		{
			assertEquals(32768 + 8192 + 2048 + 512 + 64 + 16 + 4 + 1, in.readBitsLittleEndian(16));
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			assertEquals(16384 + 4096 + 1024 + 256 + 32 + 8 + 2, in.readBitsLittleEndian(15));
		}
		finally
		{
			in.close();
		}

		in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { (byte) ((128 + 32 + 8 + 2) & 0xFF), (byte) ((64 + 16 + 4 + 1) & 0xFF) }));
		try
		{
			assertEquals(8 + 2, in.readBitsLittleEndian(4));
			assertEquals(2048 + 512 + 64 + 16 + 4 + 1, in.readBitsLittleEndian(12));
			assertTrue(in.isAtEof());
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void testCurByteIsConvertedToInt() throws IOException
	{
		// Test that a -1 byte read from the stream does not set m_curByte to -1
		LittleEndianBitInputStream in = new LittleEndianBitInputStream(new ByteArrayInputStream(new byte[] { 0, (byte) (-1 & 0xFF) }));
		try
		{
			in.readBit();
			in.readBytes(new byte[1], 0, 1);
			assertFalse(in.isAtEof());
		}
		finally
		{
			in.close();
		}
	}
}
