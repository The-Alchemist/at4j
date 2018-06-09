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

import java.io.File;

import org.at4j.test.support.At4JTestCase;
import org.at4j.test.support.TestFileSupport;
import org.entityfs.ReadWritableFile;
import org.entityfs.ReadableFile;
import org.entityfs.util.Files;
import org.entityfs.util.io.ReadWritableFileAdapter;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BZip2ReadableAndWritableFileTest extends At4JTestCase
{
	@Test
	public void testWriteAndRead()
	{
		File f = TestFileSupport.createTemporaryFile();
		try
		{
			ReadWritableFile f1 = new ReadWritableFileAdapter(f);
			BZip2WritableFile f1z = new BZip2WritableFile(f1);

			Files.writeText(f1z, "Contents of f1");

			assertEquals("Contents of f1", Files.readTextFile(new BZip2ReadableFile(f1)));
		}
		finally
		{
			TestFileSupport.deleteRecursively(f);
		}
	}

	@Test
	public void testWithMagicBytes()
	{
		ReadableFile rf = getTestDataFile("comp/bzip2/stream_w_magic_bytes.txt.bz2");
		assertEquals("Contents of stream with magic bytes.\n", Files.readTextFile(new BZip2ReadableFile(rf)));

		File f = TestFileSupport.createTemporaryFile();
		try
		{
			ReadWritableFileAdapter fa = new ReadWritableFileAdapter(f);
			Files.writeText(new BZip2WritableFile(fa, new BZip2WritableFileSettings().setBlockSize(1)), "Contents of f1");
			assertEquals("Contents of f1", Files.readTextFile(new BZip2ReadableFile(fa)));
		}
		finally
		{
			TestFileSupport.deleteRecursively(f);
		}
	}
}
