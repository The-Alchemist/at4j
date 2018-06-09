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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.at4j.test.support.At4JTestCase;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.Files;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BZip2OutputStreamTest extends At4JTestCase
{
	private void verifyRead(InputStream is, byte[] plain) throws IOException
	{
		byte[] read;
		try
		{
			read = StreamUtil.readStreamFully(is, 2048);
		}
		finally
		{
			is.close();
		}
		assertEquals(plain.length, read.length);
		// Use this loop instead of Arrays.equals to get information about where
		// the arrays first differ, if they differ.
		for (int i = 0; i < plain.length; i++)
		{
			assertEquals("" + i, plain[i], read[i]);
		}
	}

	private void verifyRead(byte[] encoded, byte[] plain) throws IOException
	{
		// AT4J's decoder
		verifyRead(new BZip2InputStream(new ByteArrayInputStream(encoded)), plain);

		// Apache Commons Compress' decoder
		verifyRead(new BZip2CompressorInputStream(new ByteArrayInputStream(encoded)), plain);
	}

	private void testWrite(byte[] data, int blockSize, int noThreads) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//		//FileOutputStream baos = new FileOutputStream(new File("/tmp/foo.txt.bz2"));
		//		OutputStream os = new BZip2CompressorOutputStream(baos, blockSize);
		//		try
		//		{
		//			os.write(data);
		//		}
		//		finally
		//		{
		//			os.close();
		//		}
		//
		//		verifyRead(baos.toByteArray(), data);

		//		baos = new ByteArrayOutputStream();
		OutputStream os = new BZip2OutputStream(baos, new BZip2OutputStreamSettings().setBlockSize(blockSize).setNumberOfEncoderThreads(noThreads));
		try
		{
			os.write(data);
		}
		finally
		{
			os.close();
		}

		verifyRead(baos.toByteArray(), data);
	}

	private void testWrite(byte[] data, int blockSize) throws IOException
	{
		for (int i = 0; i < 3; i++)
		{
			testWrite(data, blockSize, i);
		}
	}

	private void testWrite(byte[] data) throws IOException
	{
		testWrite(data, 9);
	}

	@Test
	public void testEmptyFile() throws IOException
	{
		testWrite(new byte[0]);
	}

	@Test
	public void testOneCharacter() throws IOException
	{
		testWrite(new byte[] { 'A' });
	}

	@Test
	public void testOneFancyCharacter() throws IOException
	{
		testWrite(new byte[] { -35 });
	}

	@Test
	public void testTwoCharacters() throws IOException
	{
		testWrite(new byte[] { 'A', 'B', 'B', 'A', 'B' });
	}

	@Test
	public void testTwoCharactersLen46() throws IOException
	{
		byte[] data = new byte[46];
		boolean lower = true;
		for (int j = 0; j < 46; j++)
		{
			data[j] = lower ? (byte) 'A' : (byte) 'B';
			lower = j % 3 == 0 ? lower : !lower;
		}
		testWrite(data);
	}

	@Test
	public void testSwissMiss() throws IOException
	{
		testWrite(new byte[] { 's', 'w', 'i', 's', 's', ' ', 'm', 'i', 's', 's' });
	}

	@Test
	public void testTheCompleteBookOnCheese103b() throws IOException
	{
		byte[] data = new byte[103];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese154b() throws IOException
	{
		byte[] data = new byte[154];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese104b() throws IOException
	{
		byte[] data = new byte[104];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese256b() throws IOException
	{
		byte[] data = new byte[256];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese512b() throws IOException
	{
		byte[] data = new byte[512];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese570b() throws IOException
	{
		byte[] data = new byte[570];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese768b() throws IOException
	{
		byte[] data = new byte[768];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese1024b() throws IOException
	{
		byte[] data = new byte[1024];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese1To1024b() throws IOException
	{
		byte[] data = new byte[1024];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		for (int i = 1; i <= 1024; i++)
		{
			//			System.out.println(i);
			byte[] dataToComp = new byte[i];
			System.arraycopy(data, 0, dataToComp, 0, i);
			try
			{
				testWrite(dataToComp);
			}
			catch (RuntimeException e)
			{
				System.err.println("i=" + i);
				throw e;
			}
		}
	}

	@Test
	public void testTheCompleteBookOnCheese2048b() throws IOException
	{
		byte[] data = new byte[2048];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese8192b() throws IOException
	{
		byte[] data = new byte[8192];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese16384b() throws IOException
	{
		byte[] data = new byte[16384];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese32768b() throws IOException
	{
		byte[] data = new byte[32768];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese65536b() throws IOException
	{
		byte[] data = new byte[65536];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese65537b() throws IOException
	{
		byte[] data = new byte[65537];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese84003b() throws IOException
	{
		byte[] data = new byte[84003];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese98304b() throws IOException
	{
		byte[] data = new byte[98304];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese131072b() throws IOException
	{
		byte[] data = new byte[131072];
		InputStream is = Files.openForRead(getTestDataFile("the_complete_book_on_cheese.txt"));
		try
		{
			assertEquals(data.length, is.read(data));
		}
		finally
		{
			is.close();
		}

		testWrite(data);
	}

	@Test
	public void testTheCompleteBookOnCheese() throws IOException
	{
		testWrite(Files.readBinaryFile(getTestDataFile("the_complete_book_on_cheese.txt")));
	}

	@Test
	public void testTheCompleteBookOnCheeseDifferentBlockSizes() throws IOException
	{
		// 9 is tested in the test above
		for (int i = 1; i < 9; i++)
		{
			testWrite(Files.readBinaryFile(getTestDataFile("the_complete_book_on_cheese.txt")), i);
		}
	}

	@Test
	public void testBlock9() throws IOException
	{
		// A block from a binary file that caused an error.
		testWrite(Files.readBinaryFile(getTestDataFile("comp/bzip2/block_9.dat")), 1);
	}

	@Test
	public void testBlock90To92() throws IOException
	{
		// A block from a binary file that caused an error.
		testWrite(Files.readBinaryFile(getTestDataFile("comp/bzip2/block_90_to_92.dat")), 1);
	}

	@Test
	public void testBlock375() throws IOException
	{
		// A block from a binary file that caused an error.
		testWrite(Files.readBinaryFile(getTestDataFile("comp/bzip2/block_375.dat")), 1);
	}

	@Test
	public void testBinaryFile3Mb() throws IOException
	{
		testWrite(Files.readBinaryFile(getTestDataFile("binary_file_3mb.dat")));
	}

	@Test
	public void testBinaryFile3MbDifferentBlockSizes() throws IOException
	{
		// 9 is tested in the test above
		for (int i = 1; i < 9; i++)
		{
			try
			{
				testWrite(Files.readBinaryFile(getTestDataFile("binary_file_3mb.dat")), i);
			}
			catch (RuntimeException e)
			{
				System.err.println("Block size: " + i);
				throw e;
			}
		}
	}

	@Test
	public void testRandomData() throws IOException
	{
		// Use the pseudo-random Random so that the test data will always be
		// the same
		Random r = new Random(39312);
		for (int i = 10; i < 1000000; i += 30003)
		{
			byte[] barr = new byte[i];
			r.nextBytes(barr);
			try
			{
				testWrite(barr);
			}
			catch (RuntimeException e)
			{
				throw new RuntimeException("" + i, e);
			}
		}
	}

	@Test
	public void testOneCharacterRepeated() throws IOException
	{
		testWrite(new byte[] { 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A' });
		testWrite(new byte[] { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A' });

		for (int size = 256; size < 10000000; size += 4 * size + 1)
		{
			byte[] barr = new byte[size];
			Arrays.fill(barr, (byte) 'A');
			testWrite(barr);
		}
	}

	@Test
	public void testTwoCharactersRepeated() throws IOException
	{
		int changeInterval = 2;
		for (int i = 20; i < 300000; i += (7 * i - i % 2 - 1))
		{
			byte[] data = new byte[i];
			boolean lower = true;
			for (int j = 0; j < i; j++)
			{
				data[j] = lower ? (byte) 'A' : (byte) 'B';
				lower = (j % changeInterval == 0) ? !lower : lower;
			}
			try
			{
				testWrite(data, 2);
			}
			catch (AssertionError e)
			{
				System.out.println(i);
				throw e;
			}
		}
	}

	private byte[] reverseArray(final byte[] data)
	{
		byte[] res = new byte[data.length];
		for (int i = 0; i < data.length / 2; i++)
		{
			byte tmp = res[i];
			res[i] = res[data.length - i - 1];
			res[data.length - i - 1] = tmp;
		}
		return res;
	}

	@Test
	public void testShareExecutorService() throws IOException
	{
		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		byte[] data1 = Files.readBinaryFile(getTestDataFile("binary_file_3mb.dat"));
		byte[] data2 = reverseArray(data1);

		BZip2EncoderExecutorService es = BZip2OutputStream.createExecutorService(4);
		try
		{

			OutputStream os = new BZip2OutputStream(baos1, new BZip2OutputStreamSettings().setExecutorService(es));
			try
			{
				os.write(data1);
			}
			finally
			{
				os.close();
			}

			os = new BZip2OutputStream(baos2, new BZip2OutputStreamSettings().setExecutorService(es));
			try
			{
				os.write(data2);
			}
			finally
			{
				os.close();
			}
		}
		finally
		{
			es.shutdown();
		}

		verifyRead(baos1.toByteArray(), data1);
		verifyRead(baos2.toByteArray(), data2);
	}
}
