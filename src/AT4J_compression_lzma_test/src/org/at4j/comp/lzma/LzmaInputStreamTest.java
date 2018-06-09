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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.at4j.test.support.At4JTestCase;
import org.at4j.test.support.FaultInjectionInputStream;
import org.entityfs.support.io.StreamUtil;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class LzmaInputStreamTest extends At4JTestCase
{
	@Test
	public void testEmptyStream() throws Exception
	{
		InputStream is = new LzmaInputStream(new ByteArrayInputStream(new byte[0]));
		try
		{
			// Wait a while to let the decoder thread fail
			Thread.sleep(300);
			is.read();
			fail();
		}
		catch (IOException e)
		{
			// ok
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testStreamWithInvalidContent() throws IOException
	{
		// Try to open this uncompressed stream
		InputStream is = new LzmaInputStream(getTestDataFile("the_complete_book_on_cheese.txt").openForRead());
		try
		{
			is.read();
			fail();
		}
		catch (Throwable e)
		{
			// Any kind of error may occur here. Often an OutOfMemoryError when
			// trying to allocate a large dictionary.
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testErrorPropagation() throws Exception
	{
		FaultInjectionInputStream fiis = new FaultInjectionInputStream(getTestDataFile("comp/lzma/the_complete_book_on_cheese_-_small_dictionary_size.txt.lzma").openForRead());
		InputStream is = new LzmaInputStream(fiis);
		try
		{
			is.read();
			fiis.injectFault();
			// Read so much so that the decoder has to read more from the 
			// underlying stream.
			// Dictionary size is 65536 bytes.
			byte[] barr = new byte[70000];
			try
			{
				Thread.sleep(1000);
				int noRead = 0;
				while (noRead < 70000)
				{
					noRead += is.read(barr);
				}
				fail();
			}
			catch (IOException e)
			{
				// ok
			}
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testCloseBeforeAllDataIsRead() throws IOException
	{
		InputStream is = new LzmaInputStream(getTestDataFile("comp/lzma/the_complete_book_on_cheese_-_small_dictionary_size.txt.lzma").openForRead());
		try
		{
			is.read();
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testReadStream() throws IOException
	{
		InputStream is = new LzmaInputStream(getTestDataFile("comp/lzma/the_man_without_qualities.txt.lzma").openForRead());
		try
		{
			byte[] barr = StreamUtil.readStreamFully(is, 256);
			assertEquals(new String(LzmaTestData.TEST_TEXT), new String(barr, "utf8"));
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testReadStreamCreatedByUs() throws IOException
	{
		InputStream is = new LzmaInputStream(getTestDataFile("comp/lzma/1.txt.lzma").openForRead());
		try
		{
			byte[] barr = StreamUtil.readStreamFully(is, 256);
			assertEquals(new String(LzmaTestData.TEST_TEXT), new String(barr, "utf8"));
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testReadStreamCreatedByUsWithKnownDataSize() throws IOException
	{
		InputStream is = new LzmaInputStream(getTestDataFile("comp/lzma/4.txt.lzma").openForRead());
		try
		{
			byte[] barr = StreamUtil.readStreamFully(is, 256);
			assertEquals(new String(LzmaTestData.TEST_TEXT), new String(barr, "utf8"));
		}
		finally
		{
			is.close();
		}
	}

	@Test
	public void testReadLongStream() throws IOException
	{
		String s;
		InputStream is = getTestDataFile("the_complete_book_on_cheese.txt").openForRead();
		try
		{
			s = new String(StreamUtil.readStreamFully(is, 16384), "utf8");
		}
		finally
		{
			is.close();
		}

		is = new LzmaInputStream(getTestDataFile("comp/lzma/the_complete_book_on_cheese.txt.lzma").openForRead());
		try
		{
			byte[] barr = StreamUtil.readStreamFully(is, 256);
			assertEquals(s, new String(barr, "utf8"));
		}
		finally
		{
			is.close();
		}

		is = new LzmaInputStream(getTestDataFile("comp/lzma/the_complete_book_on_cheese_-_small_dictionary_size.txt.lzma").openForRead());
		try
		{
			byte[] barr = StreamUtil.readStreamFully(is, 32768);
			assertEquals(s, new String(barr, "utf8"));
		}
		finally
		{
			is.close();
		}
	}
}
