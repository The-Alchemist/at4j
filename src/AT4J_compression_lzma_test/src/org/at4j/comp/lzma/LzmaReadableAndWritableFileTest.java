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

import java.io.File;

import org.at4j.test.support.At4JTestCase;
import org.at4j.test.support.TestFileSupport;
import org.entityfs.ReadWritableFile;
import org.entityfs.util.Files;
import org.entityfs.util.io.ReadWritableFileAdapter;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class LzmaReadableAndWritableFileTest extends At4JTestCase
{
	@Test
	public void testWriteAndRead()
	{
		File f = TestFileSupport.createTemporaryFile();
		try
		{
			ReadWritableFile f1 = new ReadWritableFileAdapter(f);
			LzmaWritableFile f1z = new LzmaWritableFile(f1);

			Files.writeText(f1z, "Contents of f1");

			assertEquals("Contents of f1", Files.readTextFile(new LzmaReadableFile(f1)));
		}
		finally
		{
			TestFileSupport.deleteRecursively(f);
		}
	}
}
