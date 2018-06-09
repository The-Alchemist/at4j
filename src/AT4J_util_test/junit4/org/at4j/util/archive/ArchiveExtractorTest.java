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
package org.at4j.util.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.at4j.archive.Archive;
import org.at4j.archive.NameGlobAEF;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.tar.TarFile;
import org.at4j.test.support.At4JTestCase;
import org.at4j.test.support.TestFileSupport;
import org.at4j.zip.ZipFile;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.EFile;
import org.entityfs.EntityView;
import org.entityfs.FileSystem;
import org.entityfs.el.RelativeLocation;
import org.entityfs.exception.DirectoryNotEmptyException;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.ostrat.DoOverwrite;
import org.entityfs.ostrat.DontOverwrite;
import org.entityfs.ostrat.OverwriteStrategy;
import org.entityfs.support.util.regexp.Glob;
import org.entityfs.util.Directories;
import org.entityfs.util.Files;
import org.junit.Test;

public class ArchiveExtractorTest extends At4JTestCase
{
	private TarFile readTar(String file)
	{
		return new TarFile(getTestDataFile(file));
	}

	private ZipFile readZip(String file)
	{
		return new ZipFile(getTestDataFile(file), Charsets.UTF8, Charsets.UTF8);
	}

	private void extract(Archive<?, ?> arc, Directory target, boolean preserveModificationTimes)
	{
		ArchiveExtractor ae = new ArchiveExtractor(arc);
		ExtractSpecification es = new ExtractSpecification();
		es.setPreserveModificationTimes(preserveModificationTimes);
		ae.extract(target, es);
	}
	
	private void extractWithOverwriteStrategy(Archive<?, ?> arc, Directory target, OverwriteStrategy ostrat)
	{
		ArchiveExtractor ae = new ArchiveExtractor(arc);
		ExtractSpecification es = new ExtractSpecification();
		es.setOverwriteStrategy(ostrat);
		ae.extract(target, es);
	}

	@Test
	public void testEmptyTar()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			TarFile tf = readTar("tar/empty.tar");
			try
			{
				extract(tf, target, false);
			}
			finally
			{
				tf.close();
			}
			assertEquals(0, Directories.size(target));
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	private void testModificationTime(EntityView ev, String modTime, boolean preserveModificationTimes)
	{
		long actualModificationTime = ev.getLastModified();
		if (preserveModificationTimes)
		{
			try
			{
				long expectedModTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(modTime).getTime();
				// One second precision
				if (Math.abs(actualModificationTime - expectedModTime) > 1000L)
				{
					fail("Expected modification time " + new Date(expectedModTime) + ", was " + new Date(actualModificationTime) + ". Using one second precision");
				}
			}
			catch (ParseException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			// Check that the modification time is in the last minute
			assertTrue(Math.abs(System.currentTimeMillis() - actualModificationTime) < 60 * 1000L);
		}
	}
	
	private void testSomeFilesAndDirectoriesInTar(boolean preserveModificationTimes)
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			TarFile tf = readTar("tar/someFilesAndDirectories.tar");
			try
			{
				extract(tf, target, preserveModificationTimes);
			}
			finally
			{
				tf.close();
			}
			//	new ArchiveExtractor(readTar("tar/someFilesAndDirectories.tar")).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(3, Directories.size(target));
			EFile f1 = Directories.getFile(target, "f1.txt");
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));
			testModificationTime(f1, "20081222181505", preserveModificationTimes);
			
			DirectoryView d2 = Directories.getDirectory(target, "d2");
			assertTrue(Directories.isEmpty(d2));
			testModificationTime(d2, "20081222181519", preserveModificationTimes);
			
			DirectoryView d1 = Directories.getDirectory(target, "d1");
			assertEquals(1, Directories.size(d1));
			testModificationTime(d1, "20081222181515", preserveModificationTimes);
			
			DirectoryView d1d = Directories.getDirectory(d1, "d");
			assertEquals(1, Directories.size(d1d));
			testModificationTime(d1d, "20081222181536", preserveModificationTimes);
			
			EFile f2 = Directories.getFile(d1d, "f2.txt");
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));
			testModificationTime(f2, "20081222181536", preserveModificationTimes);
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	@Test
	public void testSomeFilesAndDirectoriesInTar()
	{
		testSomeFilesAndDirectoriesInTar(false);
	}

	@Test
	public void testSomeFilesAndDirectoriesInTarPreserveModificationTimes()
	{
		testSomeFilesAndDirectoriesInTar(true);
	}

	private void testSomeFilesAndDirectoriesInZip(boolean preserveModificationTimes)
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			ZipFile zf = readZip("zip/InfoZipFunkyUnix.zip");
			try
			{
				extract(zf, target, preserveModificationTimes);
			}
			finally
			{
				zf.close();
			}
			// new ArchiveExtractor(readZip("zip/InfoZipFunkyUnix.zip")).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

			assertEquals(3, Directories.size(target));
			
			EFile f1 = Directories.getFile(target, "f1");
			assertEquals("Baka, baka, liten kaka\n", Files.readTextFile(f1));
			testModificationTime(f1, "20080928065522", preserveModificationTimes);
			
			DirectoryView d = Directories.getDirectory(target, "d");
			assertTrue(Directories.isEmpty(d));
			// No modification times for directories
			
			EFile shrimpSandwich = Directories.getFile(target, "räksmörgås.txt");
			assertEquals("Contents of räksmörgås\n", Files.readTextFile(shrimpSandwich, Charsets.UTF8));
			testModificationTime(shrimpSandwich, "20080928081430", preserveModificationTimes);
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}

	@Test
	public void testSomeFilesAndDirectoriesInZip()
	{
		testSomeFilesAndDirectoriesInZip(false);
	}
	
	@Test
	public void testSomeFilesAndDirectoriesInZipPreserveModificationTimes()
	{
		testSomeFilesAndDirectoriesInZip(true);
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
				TarFile tf = readTar("tar/someFilesAndDirectories.tar");
				try
				{
					extract(tf, target, false);
				}
				finally
				{
					tf.close();
				}
				//	new ArchiveExtractor(readTar("tar/someFilesAndDirectories.tar")).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

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
	public void testLockingTargetFileSystemAndFilter()
	{
		File targetDir = TestFileSupport.createTemporaryDir();
		try
		{
			FileSystem fs = new FSRWFileSystemBuilder().setRoot(targetDir).enableLocking().create();
			try
			{
				Directory target = fs.getRootDirectory();
				TarFile tf = readTar("tar/someFilesAndDirectories.tar");
				try
				{
					new ArchiveExtractor(tf).extract(target, new ExtractSpecification().setFilter(new NameGlobAEF(new Glob("f2*"))));
				}
				finally
				{
					tf.close();
				}
				//	new ArchiveExtractor(readTar("tar/someFilesAndDirectories.tar")).extract(target, new ExtractSpecification().setEntryExtractionStrategy(new PrintingEntryExtractor()));

				assertEquals(1, Directories.size(target));
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
	public void testZipFile()
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try
		{
			ZipFile zf = readZip("zip/foo.zip");
			try
			{
				extract(zf, target, false);
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	@Test
	public void testOverwriteDirectoryWhenExtractingZipFileWithDoOverwriteStrategy() 
	{
		// Extract the same Zip file to the same location twice
		Directory target = TestFileSupport.createTemporaryDirectory();
		try 
		{
			ZipFile zf = readZip("zip/7ZipWindows.zip");
			try
			{
				extractWithOverwriteStrategy(zf, target, DoOverwrite.INSTANCE);
				extractWithOverwriteStrategy(zf, target, DoOverwrite.INSTANCE);
				assertEquals("Contents of f2_deflated. Contents of f2_deflated.", Files.readTextFile(Directories.getFile(target, new RelativeLocation("d/f2_deflated"))).trim());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	@Test
	public void testOverwriteDirectoryWhenExtractingTarFileWithDoOverwriteStrategy() 
	{
		// Extract the same Tar file to the same location twice
		Directory target = TestFileSupport.createTemporaryDirectory();
		try 
		{
			TarFile tf = readTar("tar/directoryAndFile.tar");
			try
			{
				extractWithOverwriteStrategy(tf, target, DoOverwrite.INSTANCE);
				extractWithOverwriteStrategy(tf, target, DoOverwrite.INSTANCE);
				assertEquals("Contents of f", Files.readTextFile(Directories.getFile(target, new RelativeLocation("d/f"))).trim());
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	private void testOverwriteEmptyDirectoryWithDontOverwriteStrategy(Archive<?, ?> a1, Archive<?, ?> a2)
	{
		// Extract two archives that contain the same directory, but don't
		// have any colliding files.
		Directory target = TestFileSupport.createTemporaryDirectory();
		try 
		{
			extractWithOverwriteStrategy(a1, target, DontOverwrite.INSTANCE);
			extractWithOverwriteStrategy(a2, target, DontOverwrite.INSTANCE);
			assertEquals("Contents of f1", Files.readTextFile(Directories.getFile(target, new RelativeLocation("d1/f1"))).trim());
			assertEquals("Contents of f2", Files.readTextFile(Directories.getFile(target, new RelativeLocation("d1/f2"))).trim());
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	@Test
	public void testOverwriteEmptyDirectoryWhenExtractingZipFileWithDontOverwriteStrategy() 
	{
		ZipFile z1 = readZip("zip/d1f1.zip");
		try
		{
			ZipFile z2 = readZip("zip/d1f2.zip");
			try
			{
				testOverwriteEmptyDirectoryWithDontOverwriteStrategy(z1, z2);
			}
			finally
			{
				z2.close();
			}
		}
		finally
		{
			z1.close();
		}
	}
	
	@Test
	public void testOverwriteEmptyDirectoryWhenExtractingTarFileWithDontOverwriteStrategy() 
	{
		TarFile t1 = readTar("tar/d1f1.tar");
		try
		{
			TarFile t2 = readTar("tar/d1f2.tar");
			try
			{
				testOverwriteEmptyDirectoryWithDontOverwriteStrategy(t1, t2);
			}
			finally
			{
				t2.close();
			}
		}
		finally
		{
			t1.close();
		}
	}
	
	private void testOverwriteFileWithDirectoryWithDoOverwriteStrategy(Archive<?, ?> a1, Archive<?, ?> a2)
	{
		// Extract two archives that contain the same directory, but don't
		// have any colliding files.
		Directory target = TestFileSupport.createTemporaryDirectory();
		try 
		{
			extractWithOverwriteStrategy(a1, target, DoOverwrite.INSTANCE);
			extractWithOverwriteStrategy(a2, target, DoOverwrite.INSTANCE);
			assertEquals("Contents of f1", Files.readTextFile(Directories.getFile(target, new RelativeLocation("d1/f1"))).trim());
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	@Test
	public void testOverwriteFileWithDirectoryWhenExtractingZipFileWithDoOverwriteStrategy() 
	{
		ZipFile z1 = readZip("zip/d1_is_file.zip");
		try
		{
			ZipFile z2 = readZip("zip/d1f1.zip");
			try
			{
				testOverwriteFileWithDirectoryWithDoOverwriteStrategy(z1, z2);
			}
			finally
			{
				z2.close();
			}
		}
		finally
		{
			z1.close();
		}
	}
	
	@Test
	public void testOverwriteFileWithDirectoryWhenExtractingTarFileWithDoOverwriteStrategy() 
	{
		TarFile t1 = readTar("tar/d1_is_file.tar");
		try
		{
			TarFile t2 = readTar("tar/d1f1.tar");
			try
			{
				testOverwriteFileWithDirectoryWithDoOverwriteStrategy(t1, t2);
			}
			finally
			{
				t2.close();
			}
		}
		finally
		{
			t1.close();
		}
	}
	
	private void testOverwriteDirectoryWithFileWithDoOverwriteStrategy(Archive<?, ?> a1, Archive<?, ?> a2)
	{
		Directory target = TestFileSupport.createTemporaryDirectory();
		try 
		{
			extractWithOverwriteStrategy(a1, target, DoOverwrite.INSTANCE);
			extractWithOverwriteStrategy(a2, target, DoOverwrite.INSTANCE);
			fail();
		}
		catch (DirectoryNotEmptyException e)
		{
			// ok
		}
		finally
		{
			TestFileSupport.deleteRecursively(target);
		}
	}
	
	@Test
	public void testOverwriteDirectoryWithFileWhenExtractingZipFileWithDoOverwriteStrategy() 
	{
		ZipFile z1 = readZip("zip/d1f1.zip");
		try
		{
			ZipFile z2 = readZip("zip/d1_is_file.zip");
			try
			{
				testOverwriteDirectoryWithFileWithDoOverwriteStrategy(z1, z2);
			}
			finally
			{
				z2.close();
			}
		}
		finally
		{
			z1.close();
		}
	}
	
	@Test
	public void testOverwriteDirectoryWithFileWhenExtractingTarFileWithDoOverwriteStrategy() 
	{
		TarFile t1 = readTar("tar/d1f1.tar");
		try
		{
			TarFile t2 = readTar("tar/d1_is_file.tar");
			try
			{
				testOverwriteDirectoryWithFileWithDoOverwriteStrategy(t1, t2);
			}
			finally
			{
				t2.close();
			}
		}
		finally
		{
			t1.close();
		}
	}
}
