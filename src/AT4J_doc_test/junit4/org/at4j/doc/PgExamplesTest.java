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
package org.at4j.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.at4j.comp.bzip2.BZip2ReadableFile;
import org.at4j.tar.PaxDirectoryEntry;
import org.at4j.tar.PaxFileEntry;
import org.at4j.tar.TarDirectoryEntry;
import org.at4j.tar.TarFile;
import org.at4j.tar.TarFileEntry;
import org.at4j.test.support.TestFileSupport;
import org.at4j.zip.ZipDirectoryEntry;
import org.at4j.zip.ZipFile;
import org.at4j.zip.ZipFileEntry;
import org.at4j.zip.ef.UnicodePathExtraField;
import org.at4j.zip.extattrs.UnixExternalFileAttributes;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.EFile;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.support.util.TwoObjects;
import org.entityfs.util.Directories;
import org.entityfs.util.Entities;
import org.entityfs.util.Files;
import org.junit.Test;

public class PgExamplesTest extends AbstractExamplesTest
{
	private void testCADInternal(String script) throws IOException
	{
		String codePrefix = "java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		byte[] barr = (byte[]) runExampleMethodTest(script, codePrefix, codeSuffix, null, null, replaces);
		String s = new String(barr);
		assertTrue(s.startsWith("[66, 90, 104, "));
		assertTrue(s.endsWith("]Compress me!"));
	}

	@Test
	public void testCAD() throws IOException
	{
		testCADInternal("pg/ex/ex_cad.txt");
	}

	@Test
	public void testCADUsingEntityFSUtilities() throws IOException
	{
		testCADInternal("pg/ex/ex_cad_using_entityfs_utilities.txt");
	}

	private void testCADToFileInternal(String script) throws IOException
	{
		String codePrefix = "java.io.File f = (java.io.File) args[0]; java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		File f = TestFileSupport.createTemporaryFile();
		try
		{
			byte[] barr = (byte[]) runExampleMethodTest(script, codePrefix, codeSuffix, null, new Object[] { f }, replaces);
			assertEquals("Compress me!", new String(barr));
		}
		finally
		{
			assertTrue(f.delete());
		}
	}

	@Test
	public void testCADBZip2() throws IOException
	{
		testCADToFileInternal("pg/ex/ex_cad_bzip2.txt");
	}

	@Test
	public void testCADBZip2WEncoderThreads() throws IOException
	{
		String codePrefix = "java.io.File f1 = (java.io.File) args[0]; java.io.File f2 = (java.io.File) args[1]; java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		File f1 = TestFileSupport.createTemporaryFile();
		File f2 = TestFileSupport.createTemporaryFile();
		try
		{
			byte[] barr = (byte[]) runExampleMethodTest("pg/ex/ex_cad_bzip2_w_encoder_threads.txt", codePrefix, codeSuffix, null, new Object[] { f1, f2 }, replaces);
			assertEquals("Compress me!Compress me too!", new String(barr));
		}
		finally
		{
			assertTrue(f1.delete());
			assertTrue(f2.delete());
		}
	}

	@Test
	public void testCADBZip2WRWF() throws IOException
	{
		testCADToFileInternal("pg/ex/ex_cad_bzip2_w_rwf.txt");
	}

	@Test
	public void testCADGZipWRWF() throws IOException
	{
		testCADToFileInternal("pg/ex/ex_cad_gzip_w_rwf.txt");
	}

	@Test
	public void testCADLzma() throws IOException
	{
		testCADToFileInternal("pg/ex/ex_cad_lzma.txt");
	}

	@Test
	public void testCADLzmaNoConfigInFile() throws IOException
	{
		testCADToFileInternal("pg/ex/ex_cad_lzma_no_config_in_file.txt");
	}

	@Test
	public void testCADLzmaWRWF() throws IOException
	{
		testCADToFileInternal("pg/ex/ex_cad_lzma_w_rwf.txt");
	}

	@Test
	public void testArchiveExtractor() throws IOException
	{
		Directory d = TestFileSupport.createTemporaryDirectory();
		try
		{
			RandomlyAccessibleFile zf = getTestDataFile("zip/InfoZipSimpleUnix.zip");
			String codePrefix = "org.entityfs.RandomlyAccessibleFile f = (org.entityfs.RandomlyAccessibleFile) args[0]; org.entityfs.DirectoryView d = (org.entityfs.DirectoryView) args[1];";
			runExampleMethodTest("pg/ex/ex_archive_extractor.txt", codePrefix, null, null, new Object[] { zf, d }, null);
			assertEquals(3, Directories.size(d));
			DirectoryView dd = Directories.getDirectory(d, "d");
			assertTrue(Directories.isEmpty(dd));
			assertWithinLast30Seconds(new Date(Entities.getLastModified(dd)));
			EFile f1 = Directories.getFile(d, "f1_stored");
			assertEquals("Contents of f1\n", Files.readTextFile(f1));
			assertWithinLast30Seconds(new Date(Entities.getLastModified(f1)));
			EFile f2 = Directories.getFile(d, "f2_deflated");
			assertEquals("Contents of f2, contents of f2\n", Files.readTextFile(f2));
			assertWithinLast30Seconds(new Date(Entities.getLastModified(f2)));
		}
		finally
		{
			TestFileSupport.deleteRecursively(d);
		}
	}

	@Test
	public void testReadingDataFromTarArchive() throws IOException
	{
		RandomlyAccessibleFile f = getTestDataFile("tar/directoryAndFile.tar");
		String codePrefix = "org.entityfs.RandomlyAccessibleFile f = (org.entityfs.RandomlyAccessibleFile) args[0]; java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		byte[] barr = (byte[]) runExampleMethodTest("pg/ex/ex_reading_data_from_tar_archive.txt", codePrefix, codeSuffix, null, new Object[] { f }, replaces);
		assertEquals("Contents of /d: [f]Contents of f\n", new String(barr));
	}

	@Test
	public void testReadingPaxVariable() throws IOException
	{
		RandomlyAccessibleFile f = getTestDataFile("tar/paxNonAsciiCharactersInFileName.tar");
		String codePrefix = "org.entityfs.RandomlyAccessibleFile f = (org.entityfs.RandomlyAccessibleFile) args[0]; java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		String s = new String((byte[]) runExampleMethodTest("pg/ex/ex_reading_pax_variable.txt", codePrefix, codeSuffix, null, new Object[] { f }, replaces));
		assertTrue(s.contains("ctime: 1230658908.797013488"));
	}

	@Test
	public void testExtractFromTarArchive() throws IOException
	{
		Directory d = TestFileSupport.createTemporaryDirectory();
		try
		{
			RandomlyAccessibleFile f = getTestDataFile("doc/pg/differentFileTypes.tar.gz");
			String codePrefix = "org.entityfs.ReadableFile f = (org.entityfs.ReadableFile) args[0]; org.entityfs.Directory d = (org.entityfs.Directory) args[1];";
			runExampleMethodTest("pg/ex/ex_extract_from_tar_archive.txt", codePrefix, null, null, new Object[] { f, d }, null);

			assertEquals(3, Directories.size(d));
			EFile c1 = Directories.getFile(d, "Class1.java");
			assertEquals("Contents of Class1.java\n", Files.readTextFile(c1));
			assertEquals(getUtcDate("20090123145838"), new Date(c1.getLastModified()));

			DirectoryView d1 = Directories.getDirectory(d, "d");
			assertEquals(1, Directories.size(d1));
			assertWithinLast30Seconds(new Date(d1.getLastModified()));

			EFile d1c2 = Directories.getFile(d1, "Class2.java");
			assertEquals("Contents of Class2.java\n", Files.readTextFile(d1c2));
			assertEquals(getUtcDate("20090123145856"), new Date(d1c2.getLastModified()));

			DirectoryView d2 = Directories.getDirectory(d, "d2");
			assertEquals(1, Directories.size(d2));
			assertWithinLast30Seconds(new Date(d2.getLastModified()));

			EFile d2m = Directories.getFile(d2, "metadata.xml");
			assertEquals("Contents of metadata.xml\n", Files.readTextFile(d2m));
			assertEquals(getUtcDate("20090123145908"), new Date(d2m.getLastModified()));
		}
		finally
		{
			TestFileSupport.deleteRecursively(d);
		}
	}

	@Test
	public void testBuildTarWBuilder() throws IOException
	{
		Directory d = TestFileSupport.createTemporaryDirectory();
		try
		{
			DirectoryView src = Directories.newDirectory(d, "src");
			Files.writeText(Directories.newFile(src, "text.txt"), "Contents of text.txt");
			Files.writeText(Directories.newFile(src, "script.sh"), "Contents of script.sh");

			String codePrefix = "org.entityfs.Directory targetDir = (org.entityfs.Directory) args[0]; org.entityfs.Directory src = (org.entityfs.Directory) args[1];";
			runExampleMethodTest("pg/ex/ex_build_tar_w_builder.txt", codePrefix, null, null, new Object[] { d, src }, null);

			TarFile taf = new TarFile(Directories.getFile(d, "myArchive.tar"));
			try
			{
				TarDirectoryEntry td = taf.getRootEntry();
				assertEquals(2, td.getChildEntries().size());

				PaxFileEntry pf = (PaxFileEntry) td.getChildEntries().get("todays_news.xml");
				assertEquals(1234, pf.getOwnerUid());
				assertEquals("rmoore", pf.getOwnerUserName());
				assertEquals(4321, pf.getOwnerGid());
				assertEquals("bonds", pf.getOwnerGroupName());
				assertEquals(UnixEntityMode.forCode(0644), pf.getEntityMode());

				PaxDirectoryEntry pd = (PaxDirectoryEntry) td.getChildEntries().get("source");
				assertEquals(2, pd.getChildEntries().size());
				assertEquals("rmoore", pd.getOwnerUserName());
				assertEquals(4321, pd.getOwnerGid());
				assertEquals("bonds", pd.getOwnerGroupName());
				assertEquals(UnixEntityMode.forCode(0755), pd.getEntityMode());

				pf = (PaxFileEntry) pd.getChildEntries().get("text.txt");
				assertEquals(1234, pf.getOwnerUid());
				assertEquals("rmoore", pf.getOwnerUserName());
				assertEquals(4321, pf.getOwnerGid());
				assertEquals("bonds", pf.getOwnerGroupName());
				assertEquals(UnixEntityMode.forCode(0644), pf.getEntityMode());
				assertEquals("Contents of text.txt", Files.readTextFile(pf));

				pf = (PaxFileEntry) pd.getChildEntries().get("script.sh");
				assertEquals(1234, pf.getOwnerUid());
				assertEquals("rmoore", pf.getOwnerUserName());
				assertEquals(4321, pf.getOwnerGid());
				assertEquals("bonds", pf.getOwnerGroupName());
				assertEquals(UnixEntityMode.forCode(0755), pf.getEntityMode());
				assertEquals("Contents of script.sh", Files.readTextFile(pf));
			}
			finally
			{
				taf.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(d);
		}
	}

	@Test
	public void testBuildTarWStreamBuilder() throws IOException
	{
		Directory d = TestFileSupport.createTemporaryDirectory();
		try
		{
			String codePrefix = "org.entityfs.Directory targetDir = (org.entityfs.Directory) args[0];";
			runExampleMethodTest("pg/ex/ex_build_tar_w_stream_builder.txt", codePrefix, null, null, new Object[] { d }, null);

			EFile f = Directories.newFile(d, "t.tar");
			Files.copyContents(new BZip2ReadableFile(Directories.getFile(d, "myArchive.tar.bz2")), f);

			TarFile tf = new TarFile(f);
			try
			{
				TarDirectoryEntry td = tf.getRootEntry();
				assertEquals(2, td.getChildEntries().size());

				TarFileEntry fe = (TarFileEntry) td.getChildEntries().get("secret.txt");
				assertEquals(UnixEntityMode.forCode(0640), fe.getEntityMode());
				assertEquals("The contents of this file are secret!", Files.readTextFile(fe));

				fe = (TarFileEntry) td.getChildEntries().get("public.txt");
				assertEquals(UnixEntityMode.forCode(0644), fe.getEntityMode());
				assertEquals("The contents of this file are public!", Files.readTextFile(fe));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(d);
		}
	}

	@Test
	public void testReadingDataFromZipArchive() throws IOException
	{
		RandomlyAccessibleFile f = getTestDataFile("doc/pg/fileAndDirectory.zip");
		String codePrefix = "org.entityfs.RandomlyAccessibleFile f = (org.entityfs.RandomlyAccessibleFile) args[0]; java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		byte[] barr = (byte[]) runExampleMethodTest("pg/ex/ex_reading_data_from_zip_archive.txt", codePrefix, codeSuffix, null, new Object[] { f }, replaces);
		assertEquals("Contents of /d: [f]Contents of f1 åäö\n", new String(barr));
	}

	@Test
	public void testReadingMetaataFromZipEntry() throws IOException
	{
		RandomlyAccessibleFile f = getTestDataFile("zip/InfoZipFunkyUnix.zip");
		String codePrefix = "org.entityfs.RandomlyAccessibleFile f = (org.entityfs.RandomlyAccessibleFile) args[0]; java.io.ByteArrayOutputStream sbout = new java.io.ByteArrayOutputStream(); java.io.PrintWriter sout = new java.io.PrintWriter(sbout);";
		String codeSuffix = "sout.close(); return sbout.toByteArray();";
		List<TwoObjects<String, String>> replaces = new ArrayList<TwoObjects<String, String>>(1);
		replaces.add(new TwoObjects<String, String>("System.out.println(", "sout.print("));
		byte[] barr = (byte[]) runExampleMethodTest("pg/ex/ex_reading_metadata_from_zip_entry.txt", codePrefix, codeSuffix, null, new Object[] { f }, replaces);
		assertEquals("Zip file comment. Some strange characters: åäö\r\nLook Ma! A second line!Comment for f1. Some strange characters: åäöDeflated64420080928", new String(barr));
	}

	@Test
	public void testBuildZipArchive() throws IOException
	{
		Directory d = TestFileSupport.createTemporaryDirectory();
		try
		{
			DirectoryView src = Directories.newDirectory(d, "src");
			Files.writeText(Directories.newFile(src, "text.txt"), "Contents of text.txt");
			DirectoryView srcd = Directories.newDirectory(src, "d");
			Files.writeText(Directories.newFile(srcd, "script.sh"), "Contents of script.sh");

			String codePrefix = "org.entityfs.Directory targetDir = (org.entityfs.Directory) args[0]; org.entityfs.Directory src = (org.entityfs.Directory) args[1];";
			runExampleMethodTest("pg/ex/ex_build_zip.txt", codePrefix, null, null, new Object[] { d, src }, null);

			ZipFile zif = new ZipFile(Directories.getFile(d, "myArchive.zip"));
			try
			{
				assertEquals("This is myArchive.zip's comment.", zif.getComment());

				ZipDirectoryEntry zf = zif.getRootEntry();
				assertEquals(1, zf.getChildEntries().size());

				ZipDirectoryEntry de = (ZipDirectoryEntry) zf.getChildEntries().get("source");
				assertEquals(2, de.getChildEntries().size());

				ZipFileEntry fe = (ZipFileEntry) de.getChildEntries().get("text.txt");
				assertEquals("Contents of text.txt", Files.readTextFile(fe));
				assertEquals(UnixEntityMode.forCode(0644), ((UnixExternalFileAttributes) fe.getExternalFileAttributes()).getEntityMode());
				assertEquals(new AbsoluteLocation("/source/text.txt"), fe.getExtraField(UnicodePathExtraField.class, false).getAbsolutePath());

				de = (ZipDirectoryEntry) de.getChildEntries().get("d");
				assertEquals(UnixEntityMode.forCode(0755), ((UnixExternalFileAttributes) de.getExternalFileAttributes()).getEntityMode());
				assertEquals(new AbsoluteLocation("/source/d"), de.getExtraField(UnicodePathExtraField.class, false).getAbsolutePath());

				fe = (ZipFileEntry) de.getChildEntries().get("script.sh");
				assertEquals("Contents of script.sh", Files.readTextFile(fe));
				assertEquals(UnixEntityMode.forCode(0755), ((UnixExternalFileAttributes) fe.getExternalFileAttributes()).getEntityMode());
				assertEquals(new AbsoluteLocation("/source/d/script.sh"), fe.getExtraField(UnicodePathExtraField.class, false).getAbsolutePath());
			}
			finally
			{
				zif.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(d);
		}
	}

	@Test
	public void testBuildZipAndSetCompressionLevel() throws IOException
	{
		Directory d = TestFileSupport.createTemporaryDirectory();
		try
		{
			DirectoryView src = Directories.newDirectory(d, "src");
			Files.writeText(Directories.newFile(src, "text.txt"), "Contents of text.txt");
			DirectoryView srcd = Directories.newDirectory(src, "d");
			Files.writeText(Directories.newFile(srcd, "script.sh"), "Contents of script.sh");

			String codePrefix = "org.entityfs.Directory targetDir = (org.entityfs.Directory) args[0]; org.entityfs.Directory src = (org.entityfs.Directory) args[1];";
			runExampleMethodTest("pg/ex/ex_build_zip_and_set_compression_level.txt", codePrefix, null, null, new Object[] { d, src }, null);

			ZipFile zif = new ZipFile(Directories.getFile(d, "myArchive.zip"));
			try
			{
				ZipDirectoryEntry zf = zif.getRootEntry();
				assertEquals(1, zf.getChildEntries().size());

				ZipDirectoryEntry de = (ZipDirectoryEntry) zf.getChildEntries().get("source");
				assertEquals(2, de.getChildEntries().size());

				ZipFileEntry fe = (ZipFileEntry) de.getChildEntries().get("text.txt");
				assertEquals("Contents of text.txt", Files.readTextFile(fe));

				de = (ZipDirectoryEntry) de.getChildEntries().get("d");

				fe = (ZipFileEntry) de.getChildEntries().get("script.sh");
				assertEquals("Contents of script.sh", Files.readTextFile(fe));
			}
			finally
			{
				zif.close();
			}
		}
		finally
		{
			TestFileSupport.deleteRecursively(d);
		}
	}
}
