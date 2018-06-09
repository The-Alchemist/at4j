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
package org.at4j.comp.lzma;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import org.at4j.test.support.FaultInjectionOutputStream;
import org.at4j.test.support.TestFileSupport;
import org.junit.Test;

/**
 * This test class contains a main method that creates a set of files that can
 * be tested with the standalone LZMA decoding tool. Run it to get more
 * instructions.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class LzmaOutputStreamTest
{
	@Test
	public void testEmpty() throws IOException
	{
		LzmaOutputStream os = new LzmaOutputStream(new ByteArrayOutputStream());
		os.close();
	}

	@Test
	public void testErrorPropagation() throws IOException, InterruptedException
	{
		FaultInjectionOutputStream fios = new FaultInjectionOutputStream(new ByteArrayOutputStream());
		byte[] barr = new byte[16384];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(barr);
		LzmaOutputStream os = new LzmaOutputStream(fios);
		os.write(12);
		fios.injectFault();
		// Can still write.
		while(!fios.testAndClearHasThrown())
		{
			os.write(barr);
			sr.nextBytes(barr);
			
			Thread.sleep(50);
		}
		
		Thread.sleep(1000);

		// But now we should fail
		try
		{
			os.write(barr);
			fail();
		}
		catch (IOException e)
		{
			// ok
		}

		// Errors when closing should occur right away
		fios.injectFault();
		try
		{
			os.close();
			fail();
		}
		catch (IOException e)
		{
			// ok
		}
	}

	public static void main(String[] args) throws IOException
	{
		File d = TestFileSupport.createTemporaryDir();

		System.out.println("This method creates a few LZMA encoded test files. Try to decode them using the");
		System.out.println("lzcat command (or something equivalent). The files contain a quote from Robert");
		System.out.println("Musil's The Man Without Qualities.");

		// Write the text in an entire chunk
		File f1 = new File(d, "1.txt.lzma");
		LzmaOutputStream out = new LzmaOutputStream(new FileOutputStream(f1));
		try
		{
			out.write(LzmaTestData.TEST_TEXT);
		}
		finally
		{
			out.close();
		}
		System.out.println("Wrote " + f1.getAbsolutePath());

		Random rnd = new Random();
		// Write the text in chunks of random size 8 -> 24 bytes
		File f2 = new File(d, "2.txt.lzma");
		out = new LzmaOutputStream(new FileOutputStream(f2));
		try
		{
			int offset = 0;
			while (offset < LzmaTestData.TEST_TEXT.length)
			{
				int noToWrite = 8 + rnd.nextInt(16);
				noToWrite = Math.min(noToWrite, LzmaTestData.TEST_TEXT.length - offset);
				out.write(LzmaTestData.TEST_TEXT, offset, noToWrite);
				offset += noToWrite;
			}
		}
		finally
		{
			out.close();
		}
		System.out.println("Wrote " + f2.getAbsolutePath());

		// Write the text bytes, one by one
		File f3 = new File(d, "3.txt.lzma");
		out = new LzmaOutputStream(new FileOutputStream(f3), new LzmaOutputStreamSettings());
		try
		{
			for (int pos = 0; pos < LzmaTestData.TEST_TEXT.length; pos++)
			{
				out.write(LzmaTestData.TEST_TEXT[pos] & 0xFF);
			}
		}
		finally
		{
			out.close();
		}
		System.out.println("Wrote " + f3.getAbsolutePath());

		// Known data size
		File f4 = new File(d, "4.txt.lzma");
		out = new LzmaOutputStream(new FileOutputStream(f4), new LzmaOutputStreamSettings(), LzmaTestData.TEST_TEXT.length);
		try
		{
			out.write(LzmaTestData.TEST_TEXT);
		}
		finally
		{
			out.close();
		}
		System.out.println("Wrote " + f4.getAbsolutePath());
	}
}
