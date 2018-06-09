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
package org.at4j.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;

import org.at4j.support.nio.charset.Charsets;
import org.at4j.test.support.At4JTestCase;
import org.at4j.test.support.TestFileSupport;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.EFile;
import org.entityfs.FileSystem;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.support.util.regexp.Glob;
import org.entityfs.util.Directories;
import org.entityfs.util.Entities;
import org.entityfs.util.Files;
import org.junit.Test;

public class TarExtractorTest extends At4JTestCase
{
	@Test
	public void testEmptyTar()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			new TarExtractor(getTestDataFile("tar/empty.tar")).extract(target);
			assertEquals(0, Directories.size(target));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testSomeFilesAndDirectories()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			new TarExtractor(getTestDataFile("tar/someFilesAndDirectories.tar")).extract(target);
			// new ArchiveExtractor(new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(3, Directories.size(target));
			assertEquals("Contents of f1.txt\n", Files.readTextFile(Directories.getFile(target, "f1.txt")));
			assertTrue(Directories.isEmpty(Directories.getDirectory(target, "d2")));
			DirectoryView d1 = Directories.getDirectory(target, "d1");
			assertEquals(1, Directories.size(d1));
			DirectoryView d1d = Directories.getDirectory(d1, "d");
			assertEquals(1, Directories.size(d1d));
			assertEquals("Contents of f2.txt\n", Files.readTextFile(Directories.getFile(d1d, "f2.txt")));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testSomeFilesAndDirectoriesWFilter()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			new TarExtractor(getTestDataFile("tar/someFilesAndDirectories.tar")).extract(target, new TarExtractSpecification().setFilter(new TarEntryNameGlobFilter("f2*").and(TarDirectoryEntryFilter.FILTER.not())));
			// new ArchiveExtractor(new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(1, Directories.size(target));
			DirectoryView d1 = Directories.getDirectory(target, "d1");
			assertEquals(1, Directories.size(d1));
			DirectoryView d1d = Directories.getDirectory(d1, "d");
			assertEquals(1, Directories.size(d1d));
			assertEquals("Contents of f2.txt\n", Files.readTextFile(Directories.getFile(d1d, "f2.txt")));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testSymbolicLink()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			new TarExtractor(getTestDataFile("tar/paxTarLinkTargetLongerThan155Bytes.tar")).extract(target);
			fail();
		}
		catch (TarFileParseException e)
		{
			assertTrue(e.getMessage().contains("symbolic link"));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}

		target = TestFileSupport.createTemporaryDirectory();
		try
		{
			new TarExtractor(getTestDataFile("tar/paxTarLinkTargetLongerThan155Bytes.tar")).extract(target, new TarExtractSpecification().setEntryExtractionStrategy(new ExtractTarEntryExtractionStrategy(true)));
			// new ArchiveExtractor(new TarFile(getTestDataFile("tar/paxTarLinkTargetLongerThan155Bytes.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(1, Directories.size(target));
			DirectoryView d = Directories.getDirectoryMatching(target, Glob.compile("0*"));
			assertEquals(1, Directories.size(d));
			assertEquals("Contents of long file\n", Files.readTextFile(Directories.getFileMatching(d, Glob.compile("0*"))));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testOverwriteFile()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			Files.writeText(Directories.newFile(target, "test.txt"), "foo");

			new TarExtractor(getTestDataFile("tar/singleFile.tar")).extract(target);
			// new ArchiveExtractor(new TarFile(getTestDataFile("tar/singleFile.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(1, Directories.size(target));
			EFile f1 = Directories.getFile(target, "test.txt");
			assertEquals("Contents of test.txt åäö\n", Files.readTextFile(f1, Charsets.UTF8));
			assertEquals(getUtcDate("20081222092338"), new Date(Entities.getLastModified(f1)));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testOverwriteDirectory()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			Directories.newDirectory(target, "d");

			new TarExtractor(getTestDataFile("tar/singleDirectory.tar")).extract(target);
			// new ArchiveExtractor(new TarFile(getTestDataFile("tar/singleDirectory.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(1, Directories.size(target));
			assertEquals(getUtcDate("20081222170807"), new Date(Entities.getLastModified(Directories.getDirectory(target, "d"))));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testOverwriteNonEmptyDirectory()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			Directories.newDirectory(Directories.newDirectory(target, "d"), "d");
			new TarExtractor(getTestDataFile("tar/singleDirectory.tar")).extract(target);
			assertEquals(1, Directories.size(target));
			DirectoryView d = Directories.getDirectory(target, "d");
			assertEquals(getUtcDate("20081222170807"), new Date(Entities.getLastModified(d)));
			assertEquals(1, Directories.size(d));

		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testLockingTargetFileSystem()
	{
		File targetDir = TestFileSupport.createTemporaryDir();
		try
		{
			FileSystem fs = new FSRWFileSystemBuilder().setRoot(targetDir).enableLocking().create();
			try
			{
				Directory target = fs.getRootDirectory();
				new TarExtractor(getTestDataFile("tar/someFilesAndDirectories.tar")).extract(target);
				// new ArchiveExtractor(new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

				assertEquals(3, Directories.size(target));
				assertEquals("Contents of f1.txt\n", Files.readTextFile(Directories.getFile(target, "f1.txt")));
				assertTrue(Directories.isEmpty(Directories.getDirectory(target, "d2")));
				DirectoryView d1 = Directories.getDirectory(target, "d1");
				assertEquals(1, Directories.size(d1));
				DirectoryView d1d = Directories.getDirectory(d1, "d");
				assertEquals(1, Directories.size(d1d));
				assertEquals("Contents of f2.txt\n", Files.readTextFile(Directories.getFile(d1d, "f2.txt")));
			}
			finally
			{
				fs.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(targetDir);
		}
	}

	@Test
	public void testLockingSourceAndTargetFileSystem()
	{
		File targetDir = TestFileSupport.createTemporaryDir();
		try
		{
			FileSystem fs = new FSRWFileSystemBuilder().setRoot(targetDir).enableLocking().create();
			try
			{
				Directory root = fs.getRootDirectory();
				DirectoryView target = Directories.newDirectory(root, "target");
				EFile tarFile = Directories.newFile(root, "tar.tar");
				Files.copyContents(getTestDataFile("tar/someFilesAndDirectories.tar"), tarFile);
				new TarExtractor(tarFile).extract(target);
				// new ArchiveExtractor(new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"))).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

				assertEquals(3, Directories.size(target));
				assertEquals("Contents of f1.txt\n", Files.readTextFile(Directories.getFile(target, "f1.txt")));
				assertTrue(Directories.isEmpty(Directories.getDirectory(target, "d2")));
				DirectoryView d1 = Directories.getDirectory(target, "d1");
				assertEquals(1, Directories.size(d1));
				DirectoryView d1d = Directories.getDirectory(d1, "d");
				assertEquals(1, Directories.size(d1d));
				assertEquals("Contents of f2.txt\n", Files.readTextFile(Directories.getFile(d1d, "f2.txt")));
			}
			finally
			{
				fs.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(targetDir);
		}
	}
}
