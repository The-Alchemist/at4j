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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.at4j.test.support.At4JTestCase;
import org.entityfs.ReadableFile;
import org.entityfs.util.Files;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BZip2InputStreamTest extends At4JTestCase
{
	@Test
	public void testCompressedEmptyFile() throws IOException
	{
		ReadableFile rf = getTestDataFile("comp/bzip2/empty.bz2");
		BZip2InputStream is = new BZip2InputStream(Files.openForRead(rf));
		try
		{
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}
	}

	private void testFile(String filePath, byte[] expected) throws IOException
	{
		ReadableFile rf = getTestDataFile(filePath);
		BZip2InputStream is = new BZip2InputStream(Files.openForRead(rf));
		try
		{
			for (int i = 0; i < expected.length; i++)
			{
				assertEquals("" + i, expected[i] & 0xFF, is.read());
			}
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}

		BZip2CompressorInputStream ais = new BZip2CompressorInputStream(Files.openForRead(rf));
		try
		{
			for (int i = 0; i < expected.length; i++)
			{
				assertEquals("" + i, expected[i] & 0xFF, ais.read());
			}
			assertEquals(-1, ais.read());
		}
		finally
		{
			ais.close();
		}
	}

	@Test
	public void testFileContainingA() throws IOException
	{
		testFile("comp/bzip2/A.txt.bz2", new byte[] { 'A' });
	}

	@Test
	public void testFileContainingAA() throws IOException
	{
		testFile("comp/bzip2/AA.txt.bz2", new byte[] { 'A', 'A' });
	}

	@Test
	public void testFileContainingAAA() throws IOException
	{
		testFile("comp/bzip2/AAA.txt.bz2", new byte[] { 'A', 'A', 'A' });
	}

	@Test
	public void testFileContainingAAAA() throws IOException
	{
		testFile("comp/bzip2/AAAA.txt.bz2", new byte[] { 'A', 'A', 'A', 'A' });
	}

	@Test
	public void testFileContaining254As() throws IOException
	{
		byte[] expected = new byte[254];
		Arrays.fill(expected, (byte) 'A');
		testFile("comp/bzip2/A254.txt.bz2", expected);
	}

	@Test
	public void testFileContaining255As() throws IOException
	{
		// With 255 A:s in a row they don't fit in one RLE byte anymore.
		byte[] expected = new byte[255];
		Arrays.fill(expected, (byte) 'A');
		testFile("comp/bzip2/A255.txt.bz2", expected);
	}

	@Test
	public void testFileContainingOneRepeatingSymbol() throws IOException
	{
		byte[] expected = new byte[320];
		Arrays.fill(expected, (byte) 'A');
		testFile("comp/bzip2/A320.txt.bz2", expected);
	}

	@Test
	public void testFileContainingTwoSymbols() throws IOException
	{
		testFile("comp/bzip2/ABBAB.txt.bz2", new byte[] { 'A', 'B', 'B', 'A', 'B' });
	}

	@Test
	public void testFourAsAndAB() throws IOException
	{
		testFile("comp/bzip2/AAAAB.txt.bz2", new byte[] { 'A', 'A', 'A', 'A', 'B' });
	}

	@Test
	public void testSwissMiss() throws IOException
	{
		testFile("comp/bzip2/swiss_miss.txt.bz2", new byte[] { 's', 'w', 'i', 's', 's', ' ', 'm', 'i', 's', 's' });
	}

	private void testFileContainingValues(String fileName, int no) throws IOException
	{
		ReadableFile rf = getTestDataFile(fileName);
		BZip2InputStream is = new BZip2InputStream(Files.openForRead(rf));
		try
		{
			for (int i = 0; i < no; i++)
			{
				assertEquals(i, is.read());
			}
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testFileContaining100ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/100_byte_values.dat.bz2", 100);
	}

	@Test
	public void testFileContaining125ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/125_byte_values.dat.bz2", 125);
	}

	@Test
	public void testFileContaining128ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/128_byte_values.dat.bz2", 128);
	}

	@Test
	public void testFileContaining250ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/250_byte_values.dat.bz2", 250);
	}

	@Test
	public void testFileContaining254ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/254_byte_values.dat.bz2", 254);
	}

	@Test
	public void testFileContaining255ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/255_byte_values.dat.bz2", 255);
	}

	@Test
	public void testFileContaining256ByteValuesOnce() throws IOException
	{
		testFileContainingValues("comp/bzip2/256_byte_values.dat.bz2", 256);
	}

	@Test
	public void testFileContaining256ByteValuesReversedOnce() throws IOException
	{
		ReadableFile rf = getTestDataFile("comp/bzip2/256_byte_values_reversed.dat.bz2");
		BZip2InputStream is = new BZip2InputStream(Files.openForRead(rf));
		try
		{
			for (int i = 255; i >= 0; i--)
			{
				assertEquals(i, is.read());
			}
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testFileContaining256ByteValuesScrambledOnce() throws IOException
	{
		Set<Integer> s = new HashSet<Integer>(256);
		ReadableFile rf = getTestDataFile("comp/bzip2/256_byte_values_reversed.dat.bz2");
		BZip2InputStream is = new BZip2InputStream(Files.openForRead(rf));
		try
		{
			for (int i = 255; i >= 0; i--)
			{
				s.add(is.read());
			}
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}

		for (int i = 0; i <= 255; i++)
		{
			assertTrue("" + i, s.contains(i));
		}
	}

	@Test
	public void testFileContaining256ByteValuesTwice() throws IOException
	{
		ReadableFile rf = getTestDataFile("comp/bzip2/256_byte_values_twice.dat.bz2");
		BZip2InputStream is = new BZip2InputStream(Files.openForRead(rf));
		try
		{
			for (int i = 0; i <= 255; i++)
			{
				assertEquals(i, is.read());
				assertEquals(i, is.read());
			}
			assertEquals(-1, is.read());
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testTheCompleteBookOnCheese1024Bytes() throws IOException
	{
		byte[] expected = new byte[1024];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(1024, is.read(expected));
		}
		finally
		{
			is.close();
		}
		testFile("comp/bzip2/the_complete_book_on_cheese_1024b.txt.bz2", expected);
	}

	@Test
	public void testTheCompleteBookOnCheese2048Bytes() throws IOException
	{
		byte[] expected = new byte[2048];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(2048, is.read(expected));
		}
		finally
		{
			is.close();
		}
		testFile("comp/bzip2/the_complete_book_on_cheese_2048b.txt.bz2", expected);
	}

	@Test
	public void testTheCompleteBookOnCheese() throws IOException
	{
		byte[] expected = Files.readBinaryFile(getTestDataFile("the_complete_book_on_cheese.txt"));
		testFile("comp/bzip2/the_complete_book_on_cheese.txt.bz2", expected);
	}

	@Test
	public void testTheCompleteBookOnCheese100kBlockSize() throws IOException
	{
		byte[] expected = Files.readBinaryFile(getTestDataFile("the_complete_book_on_cheese.txt"));
		testFile("comp/bzip2/the_complete_book_on_cheese_100k_block_size.txt.bz2", expected);
	}

	@Test
	public void testImage() throws IOException
	{
		byte[] expected = Files.readBinaryFile(getTestDataFile("img.bmp"));
		testFile("comp/bzip2/img.bmp.bz2", expected);
	}

	@Test
	public void testImage100kBlockSize() throws IOException
	{
		byte[] expected = Files.readBinaryFile(getTestDataFile("img.bmp"));
		testFile("comp/bzip2/img_100k_block_size.bmp.bz2", expected);
	}

	@Test
	public void testFileWithInvalidBlockChecksum()
	{
		try
		{
			testFile("comp/bzip2/AAAA_invalid_block_checksum.txt.bz2", new byte[] { 'A', 'A', 'A', 'A' });
			fail();
		}
		catch (IOException e)
		{
			assertTrue(e.getMessage().contains("Invalid block checksum"));
		}
	}

	@Test
	public void testFileWithInvalidFileChecksum()
	{
		try
		{
			testFile("comp/bzip2/AAAA_invalid_file_checksum.txt.bz2", new byte[] { 'A', 'A', 'A', 'A' });
			fail();
		}
		catch (IOException e)
		{
			assertTrue(e.getMessage().contains("Invalid file checksum"));
		}
	}
}
