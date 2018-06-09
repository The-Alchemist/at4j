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
package org.at4j.zip.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

import org.at4j.archive.builder.ArchiveEntrySettingsRule;
import org.at4j.archive.builder.DirectoryETAF;
import org.at4j.archive.builder.EntityTypeETAF;
import org.at4j.archive.builder.FileETAF;
import org.at4j.archive.builder.NameGlobETAF;
import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.test.support.At4JTestCase;
import org.at4j.test.support.FaultInjectionReadableFile;
import org.at4j.test.support.TestFileSupport;
import org.at4j.zip.ZipDirectoryEntry;
import org.at4j.zip.ZipFile;
import org.at4j.zip.ZipFileEntry;
import org.at4j.zip.ZipVersionMadeBy;
import org.at4j.zip.comp.BZip2CompressionMethod;
import org.at4j.zip.comp.DeflatedCompressionMethod;
import org.at4j.zip.comp.LzmaCompressionMethod;
import org.at4j.zip.comp.StoredCompressionMethod;
import org.at4j.zip.ef.ExtendedTimestampExtraField;
import org.at4j.zip.ef.ExtendedTimestampExtraFieldFactory;
import org.at4j.zip.ef.InfoZipUnixExtraField;
import org.at4j.zip.ef.InfoZipUnixExtraFieldFactory;
import org.at4j.zip.ef.NewInfoZipUnixExtraField;
import org.at4j.zip.ef.NewInfoZipUnixExtraFieldFactory;
import org.at4j.zip.ef.UnicodeCommentExtraField;
import org.at4j.zip.ef.UnicodeCommentExtraFieldFactory;
import org.at4j.zip.ef.UnicodePathExtraField;
import org.at4j.zip.ef.UnicodePathExtraFieldFactory;
import org.at4j.zip.extattrs.MsDosExternalFileAttributes;
import org.at4j.zip.extattrs.MsDosExternalFileAttributesFactory;
import org.at4j.zip.extattrs.MsDosFileAttributes;
import org.at4j.zip.extattrs.UnixEntityType;
import org.at4j.zip.extattrs.UnixExternalFileAttributes;
import org.at4j.zip.extattrs.UnixExternalFileAttributesFactory;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.ETDirectory;
import org.entityfs.ReadWritableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.exception.EntityNotFoundException;
import org.entityfs.ram.RamFileSystemBuilder;
import org.entityfs.support.util.regexp.Glob;
import org.entityfs.util.ByteArrayReadableFile;
import org.entityfs.util.CharSequenceReadableFile;
import org.entityfs.util.Directories;
import org.entityfs.util.Files;
import org.entityfs.util.NamedReadableFileAdapter;
import org.entityfs.util.filter.entity.EntityNameFilter;
import org.entityfs.util.io.ReadWritableFileAdapter;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipBuilderTest extends At4JTestCase
{
	private ReadWritableFile createTargetFile()
	{
		return new ReadWritableFileAdapter(TestFileSupport.createTemporaryFile());
	}

	private long calcCrc32(byte[] barr)
	{
		CRC32 crc = new CRC32();
		crc.update(barr);
		return crc.getValue();
	}

	private void assertHasStandardRootDirectory(ZipFile zf)
	{
		ZipDirectoryEntry root = zf.getRootEntry();
		assertEquals(AbsoluteLocation.ROOT_DIR, root.getLocation());
		assertSame(root, zf.get(AbsoluteLocation.ROOT_DIR));
		assertNull(root.getParent());
		assertSame(ZipVersionMadeBy.MSDOS, root.getExternalFileAttributes().getVersionMadeBy());
		assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, root.getExternalFileAttributes());
		assertEquals("/", root.getName());
		assertEquals(UnsignedByte.valueOf(10), root.getVersionUsedToCreate());
		assertEquals(UnsignedByte.valueOf(10), root.getVersionNeededToExtract());
		assertFalse(root.isEncrypted());
		assertFalse(root.isStrongEncryption());
		assertWithinLast30Seconds(root.getLastModified());
		assertEquals(UnsignedShort.valueOf(0), root.getDiskNumberStart());
		assertEquals(0, root.getExtraFields().size());
		assertEquals("", root.getComment());
	}

	private void assertIsGeneratedDirectoryEntry(ZipDirectoryEntry d, AbsoluteLocation loc)
	{
		assertEquals(loc, d.getLocation());
		assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
		assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
		assertEquals(loc.getName(), d.getName());
		assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
		assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
		assertFalse(d.isEncrypted());
		assertFalse(d.isStrongEncryption());
		assertWithinLast30Seconds(d.getLastModified());
		assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
		assertEquals(0, d.getExtraFields().size());
		assertEquals("", d.getComment());
	}

	@Test
	public void testEmptyZipFile()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			new ZipBuilder(raf).close();

			ZipFile zf = new ZipFile(raf);
			assertEquals(1, zf.size());
			assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

			assertHasStandardRootDirectory(zf);
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipEmptyFile()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new ByteArrayReadableFile(new byte[0]), "test.txt"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals(0, Files.readBinaryFile(f).length);
				assertEquals(0L, f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(0L, f.getCompressedSize().longValue());
				assertEquals(0L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipSingleFile()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(contents), "test.txt"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipFileThatDoesntGetShorterWhenCompressingIt()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of test.txt"), "test.txt"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(20L, f.getCompressedSize().longValue());
				assertEquals(20L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testAddFileWithoutFileNameExtension()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of test"), "test"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test"));
				assertSame(f, root.getChildEntries().get("test"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test".getBytes()), f.getCrc32().longValue());
				assertEquals("test", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(16L, f.getCompressedSize().longValue());
				assertEquals(16L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertFalse(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipSingleFileInSubdirectory()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(contents), "test.txt"), new AbsoluteLocation("/foo/bar"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(4, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/foo"));
				assertSame(root, d.getParent());
				assertIsGeneratedDirectoryEntry(d, new AbsoluteLocation("/foo"));

				ZipDirectoryEntry d2 = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/foo/bar"));
				assertSame(d, d2.getParent());
				assertIsGeneratedDirectoryEntry(d2, new AbsoluteLocation("/foo/bar"));

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/foo/bar/test.txt"));
				assertSame(f, d2.getChildEntries().get("test.txt"));
				assertSame(d2, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipSingleFileWithComment()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(contents), "test.txt"), AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().setComment("Comment for test.txt"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("Comment for test.txt", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	private void testZipFileWithStrangeCharactersInFileNameAndCommentInternal(Charset fileNameEncodingCharset, Charset textEncodingCharset)
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";
			String comment = "Räksmörgås is shrimp sandwich in Swedish";

			ZipBuilder b = new ZipBuilder(raf, new ZipBuilderSettings().setFileNameEncodingCharset(fileNameEncodingCharset).setTextEncodingCharset(textEncodingCharset));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(contents), "räksmörgås.txt"), AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().setComment(comment));
			b.close();

			ZipFile zf = new ZipFile(raf, fileNameEncodingCharset, textEncodingCharset);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
				assertSame(f, root.getChildEntries().get("räksmörgås.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("räksmörgås.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals(comment, f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipFileWithStrangeCharactersInFileNameAndCommentInCurrentCharset()
	{
		testZipFileWithStrangeCharactersInFileNameAndCommentInternal(Charset.defaultCharset(), Charset.defaultCharset());
	}

	@Test
	public void testZipFileWithStrangeCharactersInFileNameAndCommentInIso88591()
	{
		Charset iso88591 = Charset.forName("ISO8859-1");
		testZipFileWithStrangeCharactersInFileNameAndCommentInternal(iso88591, iso88591);
	}

	@Test
	public void testZipDataFromStream()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			ZipBuilder b = new ZipBuilder(raf, new ZipBuilderSettings().setFileComment("File comment"));
			b.add(new ByteArrayInputStream("Contents of test.txt".getBytes()), new AbsoluteLocation("/test.txt"), new ZipEntrySettings().setComment("Comment for test.txt"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals("File comment", zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals("Contents of test.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(22L, f.getCompressedSize().longValue());
				// Doh! Smaller!
				assertEquals(20L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("Comment for test.txt", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipSingleDirectory()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf);
			b.add(dir);
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipDirectoryWithComment()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf);
			b.add(dir, new AbsoluteLocation("/foo"), new ZipEntrySettings().setComment("Comment for d").setCompressionMethod(DeflatedCompressionMethod.FAST_COMPRESSION));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(3, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry foo = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/foo"));
				assertSame(root, foo.getParent());
				assertSame(foo, root.getChildEntries().get("foo"));
				assertIsGeneratedDirectoryEntry(foo, new AbsoluteLocation("/foo"));

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/foo/d"));
				assertEquals(new AbsoluteLocation("/foo/d"), d.getLocation());
				assertSame(foo, d.getParent());
				assertSame(d, foo.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals("Comment for d", d.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipDirectoryAndFileInIt()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf);
			// Add the file first
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of test.txt"), "test.txt"), new AbsoluteLocation("/d"), new ZipEntrySettings().setComment("Comment for test.txt"));
			b.add(dir, AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().setComment("Comment for d").setCompressionMethod(DeflatedCompressionMethod.FAST_COMPRESSION));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(3, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("Comment for d", d.getComment());

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/test.txt"));
				assertSame(f, d.getChildEntries().get("test.txt"));
				assertSame(d, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(20L, f.getCompressedSize().longValue());
				assertEquals(20L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("Comment for test.txt", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testZipDirectoryRecursively()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory rootd = new RamFileSystemBuilder().create().getRootDirectory();
			Files.writeText(Directories.newFile(rootd, "test.txt"), "Contents of test.txt");
			DirectoryView subdd = Directories.newDirectory(rootd, "subdir");
			Files.writeText(Directories.newFile(subdd, "subdirfile"), "subdirfile subdirfile subdirfile");
			Directories.newFile(subdd, "omitted");

			UnixExternalFileAttributesFactory uefaf = new UnixExternalFileAttributesFactory(UnixEntityMode.forCode(0), UnixEntityMode.forCode(0700));

			ZipBuilder b = new ZipBuilder(raf);
			b.addRecursively(rootd, new AbsoluteLocation("/d"), new EntityNameFilter("omitted").not(), new ArchiveEntrySettingsRule<ZipEntrySettings>(new ZipEntrySettings().setComment("Comment for test.txt"), new NameGlobETAF(Glob.compile("*.txt"))),
					new ArchiveEntrySettingsRule<ZipEntrySettings>(new ZipEntrySettings().setExternalFileAttributesFactory(uefaf), new EntityTypeETAF(ETDirectory.TYPE)));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(5, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.UNIX, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, UnixEntityMode.forCode(0700)), d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/test.txt"));
				assertSame(f, d.getChildEntries().get("test.txt"));
				assertSame(d, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(20L, f.getCompressedSize().longValue());
				assertEquals(20L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("Comment for test.txt", f.getComment());

				ZipDirectoryEntry sd = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d/subdir"));
				assertEquals(new AbsoluteLocation("/d/subdir"), sd.getLocation());
				assertSame(d, sd.getParent());
				assertSame(sd, d.getChildEntries().get("subdir"));
				assertSame(ZipVersionMadeBy.UNIX, sd.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, UnixEntityMode.forCode(0700)), sd.getExternalFileAttributes());
				assertEquals("subdir", sd.getName());
				assertEquals(UnsignedByte.valueOf(10), sd.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), sd.getVersionNeededToExtract());
				assertFalse(sd.isEncrypted());
				assertFalse(sd.isStrongEncryption());
				assertWithinLast30Seconds(sd.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), sd.getDiskNumberStart());
				assertEquals(0, sd.getExtraFields().size());
				assertEquals("", sd.getComment());

				ZipFileEntry sdf = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/subdir/subdirfile"));
				assertSame(sdf, sd.getChildEntries().get("subdirfile"));
				assertSame(sd, sdf.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, sdf.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, sdf.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, sdf.getCompressionMethod());
				assertEquals("subdirfile subdirfile subdirfile", Files.readTextFile(sdf));
				assertEquals(calcCrc32("subdirfile subdirfile subdirfile".getBytes()), sdf.getCrc32().longValue());
				assertEquals("subdirfile", sdf.getName());
				assertEquals(UnsignedByte.valueOf(20), sdf.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), sdf.getVersionNeededToExtract());
				assertFalse(sdf.isEncrypted());
				assertFalse(sdf.isStrongEncryption());
				assertFalse(sdf.isCompressedPatchData());
				assertWithinLast30Seconds(sdf.getLastModified());
				assertEquals(16L, sdf.getCompressedSize().longValue());
				assertEquals(32L, sdf.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), sdf.getDiskNumberStart());
				assertFalse(sdf.isAppearingToBeTextFile());
				assertEquals(0, sdf.getExtraFields().size());
				assertEquals("", sdf.getComment());

				assertFalse(zf.containsKey(new AbsoluteLocation("/d/subdir/omitted")));
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testOverwriteFileAndDirectoryInArchive()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir1 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");
			Directory dir2 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf);
			b.setDefaultFileEntrySettings(b.getDefaultFileEntrySettings().setCompressionMethod(BZip2CompressionMethod.INSTANCE));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of first test.txt"), "test.txt"));
			b.add(dir1);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of first df1"), "f1"), new AbsoluteLocation("/d"));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of first df2"), "f2"), new AbsoluteLocation("/d"));
			// Overwrite
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of second test.txt Contents of second test.txt Contents of second test.txt"), "test.txt"));
			b.add(dir2);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of second df1"), "f1"), new AbsoluteLocation("/d"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				// The overwritten entities are not visible
				assertEquals(5, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(BZip2CompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of second test.txt Contents of second test.txt Contents of second test.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of second test.txt Contents of second test.txt Contents of second test.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(46), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(46), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(75L, f.getCompressedSize().longValue());
				assertEquals(83L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());

				ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/f1"));
				assertSame(f1, d.getChildEntries().get("f1"));
				assertSame(d, f1.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f1.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f1.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f1.getCompressionMethod());
				assertEquals("Contents of second df1", Files.readTextFile(f1));
				assertEquals(calcCrc32("Contents of second df1".getBytes()), f1.getCrc32().longValue());
				assertEquals("f1", f1.getName());
				assertEquals(UnsignedByte.valueOf(10), f1.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f1.getVersionNeededToExtract());
				assertFalse(f1.isEncrypted());
				assertFalse(f1.isStrongEncryption());
				assertFalse(f1.isCompressedPatchData());
				assertWithinLast30Seconds(f1.getLastModified());
				assertEquals(22L, f1.getCompressedSize().longValue());
				assertEquals(22L, f1.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f1.getDiskNumberStart());
				assertFalse(f1.isAppearingToBeTextFile());
				assertEquals(0, f1.getExtraFields().size());
				assertEquals("", f1.getComment());

				ZipFileEntry f2 = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/f2"));
				assertSame(f2, d.getChildEntries().get("f2"));
				assertSame(d, f2.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f2.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f2.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f2.getCompressionMethod());
				assertEquals("Contents of first df2", Files.readTextFile(f2));
				assertEquals(calcCrc32("Contents of first df2".getBytes()), f2.getCrc32().longValue());
				assertEquals("f2", f2.getName());
				assertEquals(UnsignedByte.valueOf(10), f2.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f2.getVersionNeededToExtract());
				assertFalse(f2.isEncrypted());
				assertFalse(f2.isStrongEncryption());
				assertFalse(f2.isCompressedPatchData());
				assertWithinLast30Seconds(f2.getLastModified());
				assertEquals(21L, f2.getCompressedSize().longValue());
				assertEquals(21L, f2.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f2.getDiskNumberStart());
				assertFalse(f2.isAppearingToBeTextFile());
				assertEquals(0, f2.getExtraFields().size());
				assertEquals("", f2.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testAddFileWithLzmaCompression()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(contents), "test.txt"), AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().setCompressionMethod(LzmaCompressionMethod.DEFAULT_INSTANCE));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(LzmaCompressionMethod.DEFAULT_INSTANCE, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(63), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(63), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(43L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testFileAndDirectoryWithAllSortsOfExtraFields()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf, new ZipBuilderSettings().setFileNameEncodingCharset(CP1252).setTextEncodingCharset(CP1252));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(contents), "räksmörgås.txt"), AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().addExtraFieldFactory(new InfoZipUnixExtraFieldFactory(1000, 1001))
					.addExtraFieldFactory(new NewInfoZipUnixExtraFieldFactory(1002, 1003)).addExtraFieldFactory(ExtendedTimestampExtraFieldFactory.INSTANCE).addExtraFieldFactory(UnicodeCommentExtraFieldFactory.INSTANCE)
					.addExtraFieldFactory(UnicodePathExtraFieldFactory.INSTANCE).setComment("Comment for räksmörgås.txt"));
			b.add(dir, AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().addExtraFieldFactory(new InfoZipUnixExtraFieldFactory(10000, 10001)).addExtraFieldFactory(new NewInfoZipUnixExtraFieldFactory(10002, 10003)).addExtraFieldFactory(
					ExtendedTimestampExtraFieldFactory.INSTANCE).addExtraFieldFactory(UnicodeCommentExtraFieldFactory.INSTANCE).addExtraFieldFactory(UnicodePathExtraFieldFactory.INSTANCE));
			b.close();

			ZipFile zf = new ZipFile(raf, CP1252, CP1252);
			try
			{
				assertEquals(3, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
				assertSame(f, root.getChildEntries().get("räksmörgås.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("räksmörgås.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(10, f.getExtraFields().size());

				ExtendedTimestampExtraField etef = f.getExtraField(ExtendedTimestampExtraField.class, true);
				assertWithinLast30Seconds(etef.getLastModified());
				etef = f.getExtraField(ExtendedTimestampExtraField.class, false);
				assertWithinLast30Seconds(etef.getLastModified());

				InfoZipUnixExtraField izuef = f.getExtraField(InfoZipUnixExtraField.class, true);
				assertWithinLast30Seconds(izuef.getLastModificationTime());
				assertWithinLast30Seconds(izuef.getLastAccessTime());
				assertEquals(1000, izuef.getUid().intValue());
				assertEquals(1001, izuef.getGid().intValue());
				izuef = f.getExtraField(InfoZipUnixExtraField.class, false);
				assertWithinLast30Seconds(izuef.getLastModificationTime());
				assertWithinLast30Seconds(izuef.getLastAccessTime());
				assertNull(izuef.getUid());
				assertNull(izuef.getGid());

				NewInfoZipUnixExtraField nizuef = f.getExtraField(NewInfoZipUnixExtraField.class, true);
				assertEquals(1002, nizuef.getUid().intValue());
				assertEquals(1003, nizuef.getGid().intValue());
				nizuef = f.getExtraField(NewInfoZipUnixExtraField.class, false);
				assertNull(nizuef.getUid());
				assertNull(nizuef.getGid());

				UnicodeCommentExtraField ucef = f.getExtraField(UnicodeCommentExtraField.class, true);
				assertEquals("Comment for räksmörgås.txt", ucef.getComment());
				ucef = f.getExtraField(UnicodeCommentExtraField.class, false);
				assertEquals("Comment for räksmörgås.txt", ucef.getComment());

				UnicodePathExtraField upef = f.getExtraField(UnicodePathExtraField.class, true);
				assertEquals(new AbsoluteLocation("/räksmörgås.txt"), upef.getAbsolutePath());
				upef = f.getExtraField(UnicodePathExtraField.class, false);
				assertEquals(new AbsoluteLocation("/räksmörgås.txt"), upef.getAbsolutePath());

				assertEquals("Comment for räksmörgås.txt", f.getComment());

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(10, d.getExtraFields().size());

				etef = d.getExtraField(ExtendedTimestampExtraField.class, true);
				assertWithinLast30Seconds(etef.getLastModified());
				etef = d.getExtraField(ExtendedTimestampExtraField.class, false);
				assertWithinLast30Seconds(etef.getLastModified());

				izuef = d.getExtraField(InfoZipUnixExtraField.class, true);
				assertWithinLast30Seconds(izuef.getLastModificationTime());
				assertWithinLast30Seconds(izuef.getLastAccessTime());
				assertEquals(10000, izuef.getUid().intValue());
				assertEquals(10001, izuef.getGid().intValue());
				izuef = d.getExtraField(InfoZipUnixExtraField.class, false);
				assertWithinLast30Seconds(izuef.getLastModificationTime());
				assertWithinLast30Seconds(izuef.getLastAccessTime());
				assertNull(izuef.getUid());
				assertNull(izuef.getGid());

				nizuef = d.getExtraField(NewInfoZipUnixExtraField.class, true);
				assertEquals(10002, nizuef.getUid().intValue());
				assertEquals(10003, nizuef.getGid().intValue());
				nizuef = d.getExtraField(NewInfoZipUnixExtraField.class, false);
				assertNull(nizuef.getUid());
				assertNull(nizuef.getGid());

				ucef = d.getExtraField(UnicodeCommentExtraField.class, true);
				assertEquals("", ucef.getComment());
				ucef = d.getExtraField(UnicodeCommentExtraField.class, false);
				assertEquals("", ucef.getComment());

				upef = d.getExtraField(UnicodePathExtraField.class, true);
				assertEquals(new AbsoluteLocation("/d"), upef.getAbsolutePath());
				upef = d.getExtraField(UnicodePathExtraField.class, false);
				assertEquals(new AbsoluteLocation("/d"), upef.getAbsolutePath());

				assertEquals("", d.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipJavaIoFileFile()
	{
		ReadWritableFile raf = createTargetFile();
		File dir = TestFileSupport.createTemporaryDir();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			File ff = new File(dir, "test.txt");
			Files.writeText(new ReadWritableFileAdapter(ff), contents);

			ZipBuilder b = new ZipBuilder(raf);
			b.add(ff);
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir);
		}
	}

	@Test
	public void testZipJavaIoFileDirectory()
	{
		ReadWritableFile raf = createTargetFile();
		File dir = TestFileSupport.createTemporaryDir();
		try
		{
			File dird = new File(dir, "d");
			assertTrue(dird.mkdir());

			ZipBuilder b = new ZipBuilder(raf);
			b.add(dird);
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir);
		}
	}

	@Test
	public void testZipNonexistantJavaIoFile()
	{
		File f = TestFileSupport.createTemporaryFile();
		assertTrue(f.delete());

		ReadWritableFile raf = createTargetFile();
		try
		{
			ZipBuilder b = new ZipBuilder(raf);
			try
			{
				b.add(f);
				fail();
			}
			catch (EntityNotFoundException e)
			{
				// ok
			}
			b.close();
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipJavaIoFileFileRecursively()
	{
		ReadWritableFile raf = createTargetFile();
		File dir = TestFileSupport.createTemporaryDir();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			File ff = new File(dir, "test.txt");
			Files.writeText(new ReadWritableFileAdapter(ff), contents);

			ZipBuilder b = new ZipBuilder(raf);
			b.addRecursively(ff, AbsoluteLocation.ROOT_DIR);
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(2, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir);
		}
	}

	@Test
	public void testZipJavaIoFileDirectoryRecursively()
	{
		ReadWritableFile raf = createTargetFile();
		File dir = TestFileSupport.createTemporaryDir();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			File dird = new File(dir, "d");
			assertTrue(dird.mkdir());
			File dirdf = new File(dird, "test.txt");
			Files.writeText(new ReadWritableFileAdapter(dirdf), contents);

			ZipBuilder b = new ZipBuilder(raf);
			b.addRecursively(dird, new AbsoluteLocation("/d"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(3, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/test.txt"));
				assertSame(f, d.getChildEntries().get("test.txt"));
				assertSame(d, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir);
		}
	}

	@Test
	public void testZipNonexistantJavaIoFileRecursively()
	{
		File f = TestFileSupport.createTemporaryFile();
		assertTrue(f.delete());
		ReadWritableFile raf = createTargetFile();
		try
		{
			ZipBuilder b = new ZipBuilder(raf);
			try
			{
				b.addRecursively(f, new AbsoluteLocation("/d"));
				fail();
			}
			catch (EntityNotFoundException e)
			{
				// ok
			}
			b.close();
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testAddRootDirRecursively()
	{
		ReadWritableFile raf = createTargetFile();
		File dir = TestFileSupport.createTemporaryDir();
		try
		{
			// This can be compressed
			String contents = "Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt. Contents of test.txt.";

			File dird = new File(dir, "d");
			assertTrue(dird.mkdir());
			File dirdf = new File(dird, "test.txt");
			Files.writeText(new ReadWritableFileAdapter(dirdf), contents);

			Directory dir1 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");
			Files.writeText(Directories.newFile(dir1, "test2.txt"), "Contents of test2.txt");

			ZipBuilder b = new ZipBuilder(raf);
			b.addRecursively(dird, AbsoluteLocation.ROOT_DIR);
			b.addRecursively(dir1, AbsoluteLocation.ROOT_DIR);
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(3, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
				assertEquals(contents, Files.readTextFile(f));
				assertEquals(calcCrc32(contents.getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(27L, f.getCompressedSize().longValue());
				assertEquals(109L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());

				f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test2.txt"));
				assertSame(f, root.getChildEntries().get("test2.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test2.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test2.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test2.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(21L, f.getCompressedSize().longValue());
				assertEquals(21L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir);
		}
	}

	@Test
	public void testGlobalRules()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir1 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d1");
			Directory dir2 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d2");

			ZipBuilder b = new ZipBuilder(raf);
			b.addRule(new ArchiveEntrySettingsRule<ZipEntrySettings>(new ZipEntrySettings().setExternalFileAttributesFactory(new UnixExternalFileAttributesFactory(UnixEntityMode.forCode(0755), UnixEntityMode.forCode(0))).setComment("Script file comment"),
					FileETAF.FILTER.and(new NameGlobETAF("*.sh"))));
			b.addRule(new ArchiveEntrySettingsRule<ZipEntrySettings>(new ZipEntrySettings().setExternalFileAttributesFactory(new MsDosExternalFileAttributesFactory(new MsDosExternalFileAttributes(MsDosFileAttributes.ARCHIVE),
					new MsDosExternalFileAttributes(MsDosFileAttributes.VOLUME_LABEL))), DirectoryETAF.FILTER.and(new NameGlobETAF("?1"))));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of test.txt"), "test.txt"));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of test.sh"), "test.sh"));
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of test2.sh"), "test2.sh"), AbsoluteLocation.ROOT_DIR, new ZipEntrySettings()
					.setExternalFileAttributesFactory(MsDosExternalFileAttributesFactory.DEFAULT_INSTANCE));
			b.add(dir1);
			b.add(dir2);
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(6, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
				assertSame(f, root.getChildEntries().get("test.txt"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test.txt", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test.txt".getBytes()), f.getCrc32().longValue());
				assertEquals("test.txt", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(20L, f.getCompressedSize().longValue());
				assertEquals(20L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("", f.getComment());

				f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.sh"));
				assertSame(f, root.getChildEntries().get("test.sh"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.UNIX, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0755)), f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test.sh", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test.sh".getBytes()), f.getCrc32().longValue());
				assertEquals("test.sh", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(19L, f.getCompressedSize().longValue());
				assertEquals(19L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("Script file comment", f.getComment());

				f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test2.sh"));
				assertSame(f, root.getChildEntries().get("test2.sh"));
				assertSame(root, f.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
				assertEquals("Contents of test2.sh", Files.readTextFile(f));
				assertEquals(calcCrc32("Contents of test2.sh".getBytes()), f.getCrc32().longValue());
				assertEquals("test2.sh", f.getName());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
				assertFalse(f.isEncrypted());
				assertFalse(f.isStrongEncryption());
				assertFalse(f.isCompressedPatchData());
				assertWithinLast30Seconds(f.getLastModified());
				assertEquals(20L, f.getCompressedSize().longValue());
				assertEquals(20L, f.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
				assertTrue(f.isAppearingToBeTextFile());
				assertEquals(0, f.getExtraFields().size());
				assertEquals("Script file comment", f.getComment());

				ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d1"));
				assertEquals(new AbsoluteLocation("/d1"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d1"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(new MsDosExternalFileAttributes(MsDosFileAttributes.VOLUME_LABEL), d.getExternalFileAttributes());
				assertEquals("d1", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());

				d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d2"));
				assertEquals(new AbsoluteLocation("/d2"), d.getLocation());
				assertSame(root, d.getParent());
				assertSame(d, root.getChildEntries().get("d2"));
				assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
				assertEquals("d2", d.getName());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
				assertFalse(d.isEncrypted());
				assertFalse(d.isStrongEncryption());
				assertWithinLast30Seconds(d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
				assertEquals(0, d.getExtraFields().size());
				assertEquals("", d.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testZipWithMissingIntermediateDirectories()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"), new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
			b.add(dir, new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(5, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipDirectoryEntry d1 = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file"), d1.getLocation());
				assertSame(root, d1.getParent());
				assertSame(d1, root.getChildEntries().get("nonexisting_parent_dir_for_file"));
				assertSame(ZipVersionMadeBy.MSDOS, d1.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d1.getExternalFileAttributes());
				assertEquals("nonexisting_parent_dir_for_file", d1.getName());
				assertEquals(UnsignedByte.valueOf(10), d1.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d1.getVersionNeededToExtract());
				assertFalse(d1.isEncrypted());
				assertFalse(d1.isStrongEncryption());
				assertWithinLast30Seconds(d1.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d1.getDiskNumberStart());
				assertEquals(0, d1.getExtraFields().size());
				assertEquals("", d1.getComment());
				assertEquals(1, d1.getChildEntries().size());

				ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file/f1.txt"));
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file/f1.txt"), f1.getLocation());
				assertSame(f1, d1.getChildEntries().get("f1.txt"));
				assertSame(d1, f1.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f1.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f1.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f1.getCompressionMethod());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));
				assertEquals(calcCrc32("Contents of f1.txt".getBytes()), f1.getCrc32().longValue());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnsignedByte.valueOf(10), f1.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f1.getVersionNeededToExtract());
				assertFalse(f1.isEncrypted());
				assertFalse(f1.isStrongEncryption());
				assertFalse(f1.isCompressedPatchData());
				assertWithinLast30Seconds(f1.getLastModified());
				assertEquals(18L, f1.getCompressedSize().longValue());
				assertEquals(18L, f1.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f1.getDiskNumberStart());
				assertTrue(f1.isAppearingToBeTextFile());
				assertEquals(0, f1.getExtraFields().size());
				assertEquals("", f1.getComment());

				ZipDirectoryEntry d2 = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"), d2.getLocation());
				assertSame(root, d2.getParent());
				assertSame(d2, root.getChildEntries().get("nonexisting_parent_dir_for_directory"));
				assertSame(ZipVersionMadeBy.MSDOS, d2.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d2.getExternalFileAttributes());
				assertEquals("nonexisting_parent_dir_for_directory", d2.getName());
				assertEquals(UnsignedByte.valueOf(10), d2.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d2.getVersionNeededToExtract());
				assertFalse(d2.isEncrypted());
				assertFalse(d2.isStrongEncryption());
				assertWithinLast30Seconds(d2.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d2.getDiskNumberStart());
				assertEquals(0, d2.getExtraFields().size());
				assertEquals("", d2.getComment());
				assertEquals(1, d2.getChildEntries().size());

				ZipDirectoryEntry d2d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"));
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"), d2d.getLocation());
				assertSame(d2, d2d.getParent());
				assertSame(d2d, d2.getChildEntries().get("d"));
				assertSame(ZipVersionMadeBy.MSDOS, d2d.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d2d.getExternalFileAttributes());
				assertEquals("d", d2d.getName());
				assertEquals(UnsignedByte.valueOf(10), d2d.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), d2d.getVersionNeededToExtract());
				assertFalse(d2d.isEncrypted());
				assertFalse(d2d.isStrongEncryption());
				assertWithinLast30Seconds(d2d.getLastModified());
				assertEquals(UnsignedShort.valueOf(0), d2d.getDiskNumberStart());
				assertEquals(0, d2d.getExtraFields().size());
				assertEquals("", d2d.getComment());
				assertEquals(0, d2d.getChildEntries().size());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testThatArchiveIsNotCorruptedWhenFailingToAddFiles()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			ZipBuilder b = new ZipBuilder(raf);
			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"));

			try
			{
				// Failing when writing extra fields
				b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f2.txt"), "f2.txt"), AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().addExtraFieldFactory(new FaultInjectionZipEntryExtraFieldFactory()));
				fail();
			}
			catch (RuntimeException e)
			{
				assertEquals("Injected error", e.getMessage());
			}
			try
			{
				// Failing when writing extra fields
				b.add(dir, AbsoluteLocation.ROOT_DIR, new ZipEntrySettings().addExtraFieldFactory(new FaultInjectionZipEntryExtraFieldFactory()));
				fail();
			}
			catch (RuntimeException e)
			{
				assertEquals("Injected error", e.getMessage());
			}

			try
			{
				// Failing when writing the file
				b.add(new NamedReadableFileAdapter(new FaultInjectionReadableFile(), "f3.txt"));
				fail();
			}
			catch (RuntimeException e)
			{
				assertEquals("Injected fault", e.getCause().getMessage());
			}

			b.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f4.txt"), "f4.txt"));
			b.add(dir);

			b.close();

			ZipFile zf = new ZipFile(raf);
			try
			{
				assertEquals(4, zf.size());
				assertEquals(ZipBuilderSettings.DEFAULT_FILE_COMMENT, zf.getComment());

				ZipDirectoryEntry root = zf.getRootEntry();
				assertHasStandardRootDirectory(zf);

				ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f1.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f1.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f1.getCompressionMethod());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));
				assertEquals(calcCrc32("Contents of f1.txt".getBytes()), f1.getCrc32().longValue());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnsignedByte.valueOf(10), f1.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f1.getVersionNeededToExtract());
				assertFalse(f1.isEncrypted());
				assertFalse(f1.isStrongEncryption());
				assertFalse(f1.isCompressedPatchData());
				assertWithinLast30Seconds(f1.getLastModified());
				assertEquals(18L, f1.getCompressedSize().longValue());
				assertEquals(18L, f1.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f1.getDiskNumberStart());
				assertTrue(f1.isAppearingToBeTextFile());
				assertEquals(0, f1.getExtraFields().size());
				assertEquals("", f1.getComment());

				ZipFileEntry f4 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f4.txt"));
				assertSame(f4, root.getChildEntries().get("f4.txt"));
				assertSame(root, f4.getParent());
				assertSame(ZipVersionMadeBy.MSDOS, f4.getExternalFileAttributes().getVersionMadeBy());
				assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f4.getExternalFileAttributes());
				assertSame(StoredCompressionMethod.INSTANCE, f4.getCompressionMethod());
				assertEquals("Contents of f4.txt", Files.readTextFile(f4));
				assertEquals(calcCrc32("Contents of f4.txt".getBytes()), f4.getCrc32().longValue());
				assertEquals("f4.txt", f4.getName());
				assertEquals(UnsignedByte.valueOf(10), f4.getVersionUsedToCreate());
				assertEquals(UnsignedByte.valueOf(10), f4.getVersionNeededToExtract());
				assertFalse(f4.isEncrypted());
				assertFalse(f4.isStrongEncryption());
				assertFalse(f4.isCompressedPatchData());
				assertWithinLast30Seconds(f4.getLastModified());
				assertEquals(18L, f4.getCompressedSize().longValue());
				assertEquals(18L, f4.getUncompressedSize().longValue());
				assertEquals(UnsignedShort.valueOf(0), f4.getDiskNumberStart());
				assertTrue(f4.isAppearingToBeTextFile());
				assertEquals(0, f4.getExtraFields().size());
				assertEquals("", f4.getComment());
			}
			finally
			{
				zf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}
}
