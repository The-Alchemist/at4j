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
package org.at4j.zip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.EnumSet;

import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.support.util.WinNtTime;
import org.at4j.test.support.At4JTestCase;
import org.at4j.zip.comp.BZip2CompressionMethod;
import org.at4j.zip.comp.DeflatedCompressionMethod;
import org.at4j.zip.comp.LzmaCompressionMethod;
import org.at4j.zip.comp.StoredCompressionMethod;
import org.at4j.zip.ef.ExtendedTimestampExtraField;
import org.at4j.zip.ef.InfoZipUnixExtraField;
import org.at4j.zip.ef.NewInfoZipUnixExtraField;
import org.at4j.zip.ef.NtfsExtraField;
import org.at4j.zip.ef.UnicodePathExtraField;
import org.at4j.zip.ef.ZipEntryExtraField;
import org.at4j.zip.extattrs.MsDosExternalFileAttributes;
import org.at4j.zip.extattrs.MsDosFileAttributes;
import org.at4j.zip.extattrs.NtfsExternalFileAttributes;
import org.at4j.zip.extattrs.NtfsFileAttributes;
import org.at4j.zip.extattrs.UnixEntityType;
import org.at4j.zip.extattrs.UnixExternalFileAttributes;
import org.entityfs.RandomAccess;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.el.RelativeLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.exception.ReadOnlyException;
import org.entityfs.support.io.RandomAccessMode;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.Files;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipFileTest extends At4JTestCase
{
	@Test
	public void testEmptyFile()
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/empty.zip"));
		try
		{
			assertEquals(1, zf.size());
			assertEquals("", zf.getComment());
			ZipDirectoryEntry rootDir = (ZipDirectoryEntry) zf.get(AbsoluteLocation.ROOT_DIR);
			assertEquals(0, rootDir.getChildEntries().size());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testTextFile()
	{
		try
		{
			new ZipFile(getTestDataFile("zip/text.txt"));
			fail();
		}
		catch (ZipFileParseException e)
		{
			// ok
		}
	}

	private void assertHasExtraField(ZipEntry ze, ZipEntryExtraField f)
	{
		for (ZipEntryExtraField zeef : ze.getExtraFields())
		{
			if (zeef.equals(f))
			{
				return;
			}
		}
		fail("Did not find extra field " + f);
	}

	@Test
	public void testSimpleUnix() throws Exception
	{
		Date nowIsh = new Date();

		ZipFile zf = new ZipFile(getTestDataFile("zip/InfoZipSimpleUnix.zip"));
		try
		{
			assertEquals(4, zf.size());
			assertEquals("", zf.getComment());

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
			assertTrue(root.getLastModified().getTime() >= nowIsh.getTime());
			assertEquals(UnsignedShort.valueOf(0), root.getDiskNumberStart());
			assertEquals("", root.getComment());

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
			assertTrue(root.getChildEntries().containsValue(d));
			assertSame(root, d.getParent());
			assertSame(ZipVersionMadeBy.UNIX, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, UnixEntityMode.forCode(0755)), d.getExternalFileAttributes());
			assertEquals("d", d.getName());
			assertEquals(UnsignedByte.valueOf(23), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(getLocalDate("20080921173548"), d.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals(4, d.getExtraFields().size());
			assertHasExtraField(d, new ExtendedTimestampExtraField(true, getUtcDate("20080921153547"), getUtcDate("20080921153547"), null));
			assertHasExtraField(d, new ExtendedTimestampExtraField(false, getUtcDate("20080921153547"), null, null));
			assertHasExtraField(d, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(d, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(1000)));
			assertEquals("", d.getComment());

			ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1_stored"));
			assertTrue(root.getChildEntries().containsValue(f1));
			assertSame(root, f1.getParent());
			assertSame(ZipVersionMadeBy.UNIX, f1.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f1.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f1.getCompressionMethod());
			assertEquals("Contents of f1\n", Files.readTextFile(f1));
			assertEquals(UnsignedInteger.valueOf(0x8ef5bfb4L), f1.getCrc32());
			assertEquals("f1_stored", f1.getName());
			assertEquals(UnsignedByte.valueOf(23), f1.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f1.getVersionNeededToExtract());
			assertFalse(f1.isEncrypted());
			assertFalse(f1.isStrongEncryption());
			assertFalse(f1.isCompressedPatchData());
			assertEquals(getLocalDate("20080921173616"), f1.getLastModified());
			assertEquals(15L, f1.getCompressedSize().longValue());
			assertEquals(15L, f1.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f1.getDiskNumberStart());
			assertTrue(f1.isAppearingToBeTextFile());
			assertEquals(4, f1.getExtraFields().size());
			assertHasExtraField(f1, new ExtendedTimestampExtraField(true, getUtcDate("20080921153616"), getUtcDate("20080921153632"), null));
			assertHasExtraField(f1, new ExtendedTimestampExtraField(false, getUtcDate("20080921153616"), null, null));
			assertHasExtraField(f1, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(f1, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(1000)));
			assertEquals("", f1.getComment());

			ZipFileEntry f2 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f2_deflated"));
			assertTrue(root.getChildEntries().containsValue(f2));
			assertSame(root, f2.getParent());
			assertSame(ZipVersionMadeBy.UNIX, f2.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f2.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f2.getCompressionMethod());
			assertEquals("Contents of f2, contents of f2\n", Files.readTextFile(f2));
			assertEquals(UnsignedInteger.valueOf(0x7a14d4bcL), f2.getCrc32());
			assertEquals("f2_deflated", f2.getName());
			assertEquals(UnsignedByte.valueOf(23), f2.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f2.getVersionNeededToExtract());
			assertFalse(f2.isEncrypted());
			assertFalse(f2.isStrongEncryption());
			assertFalse(f2.isCompressedPatchData());
			assertEquals(getLocalDate("20080921173630"), f2.getLastModified());
			assertEquals(22L, f2.getCompressedSize().longValue());
			assertEquals(31L, f2.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f2.getDiskNumberStart());
			assertTrue(f2.isAppearingToBeTextFile());
			assertEquals(4, f2.getExtraFields().size());
			assertHasExtraField(f2, new ExtendedTimestampExtraField(true, getUtcDate("20080921153630"), getUtcDate("20080921153632"), null));
			assertHasExtraField(f2, new ExtendedTimestampExtraField(false, getUtcDate("20080921153630"), null, null));
			assertHasExtraField(f2, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(f2, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(1000)));
			assertEquals("", f2.getComment());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testFunkyUnix() throws Exception
	{
		Date nowIsh = new Date();

		ZipFile zf = new ZipFile(getTestDataFile("zip/InfoZipFunkyUnix.zip"), Charsets.UTF8, Charsets.UTF8);
		try
		{
			assertEquals(6, zf.size());
			assertEquals("Zip file comment. Some strange characters: åäö\r\nLook Ma! A second line!", zf.getComment());

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
			assertTrue(root.getLastModified().getTime() >= nowIsh.getTime());
			assertEquals(UnsignedShort.valueOf(0), root.getDiskNumberStart());
			assertEquals("", root.getComment());

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertSame(d, zf.get(new AbsoluteLocation("/d")));
			assertSame(root, d.getParent());
			assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
			assertEquals("d", d.getName());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertTrue(d.getLastModified().getTime() >= nowIsh.getTime());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());

			ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1"));
			assertSame(f1, root.getChildEntries().get("f1"));
			assertSame(root, f1.getParent());
			assertSame(ZipVersionMadeBy.UNIX, f1.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f1.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.MAXIMUM_COMPRESSION, f1.getCompressionMethod());
			assertEquals("Baka, baka, liten kaka\n", Files.readTextFile(f1));
			assertEquals(UnsignedInteger.valueOf(0x8421d658L), f1.getCrc32());
			assertEquals("f1", f1.getName());
			assertEquals(UnsignedByte.valueOf(23), f1.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f1.getVersionNeededToExtract());
			assertFalse(f1.isEncrypted());
			assertFalse(f1.isStrongEncryption());
			assertFalse(f1.isCompressedPatchData());
			assertEquals(getLocalDate("20080928065522"), f1.getLastModified());
			assertEquals(20L, f1.getCompressedSize().longValue());
			assertEquals(23L, f1.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f1.getDiskNumberStart());
			assertTrue(f1.isAppearingToBeTextFile());
			assertEquals(4, f1.getExtraFields().size());
			assertHasExtraField(f1, new ExtendedTimestampExtraField(true, getUtcDate("20080928045522"), getUtcDate("20080928053232"), null));
			assertHasExtraField(f1, new ExtendedTimestampExtraField(false, getUtcDate("20080928045522"), null, null));
			assertHasExtraField(f1, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(f1, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(20000)));
			assertEquals("Comment for f1. Some strange characters: åäö", f1.getComment());

			ZipSymbolicLinkEntry f1l = (ZipSymbolicLinkEntry) zf.get(new AbsoluteLocation("/d/f1"));
			assertSame(f1l, d.getChildEntries().get("f1"));
			assertSame(d, f1l.getParent());
			assertSame(ZipVersionMadeBy.UNIX, f1l.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.SYMBOLIC_LINK, UnixEntityMode.forCode(0777)), f1l.getExternalFileAttributes());
			assertEquals(UnsignedInteger.valueOf(0xa95673b3L), f1l.getCrc32());
			assertEquals("f1", f1l.getName());
			assertEquals(UnsignedByte.valueOf(23), f1l.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f1l.getVersionNeededToExtract());
			assertFalse(f1l.isEncrypted());
			assertFalse(f1l.isStrongEncryption());
			assertEquals(getLocalDate("20080928065510"), f1l.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), f1l.getDiskNumberStart());
			assertEquals(4, f1l.getExtraFields().size());
			assertHasExtraField(f1l, new ExtendedTimestampExtraField(true, getUtcDate("20080928045509"), getUtcDate("20080928045955"), null));
			assertHasExtraField(f1l, new ExtendedTimestampExtraField(false, getUtcDate("20080928045509"), null, null));
			assertHasExtraField(f1l, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(f1l, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(1000)));
			assertEquals("Comment for the relative cymbalic link.", f1l.getComment());
			assertEquals(new RelativeLocation("../f1"), f1l.getLinkTarget());

			ZipSymbolicLinkEntry tmpl = (ZipSymbolicLinkEntry) zf.get(new AbsoluteLocation("/tmp"));
			assertSame(tmpl, root.getChildEntries().get("tmp"));
			assertSame(root, tmpl.getParent());
			assertSame(ZipVersionMadeBy.UNIX, tmpl.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.SYMBOLIC_LINK, UnixEntityMode.forCode(0777)), tmpl.getExternalFileAttributes());
			assertEquals(UnsignedInteger.valueOf(0x0abbc42eL), tmpl.getCrc32());
			assertEquals("tmp", tmpl.getName());
			assertEquals(UnsignedByte.valueOf(23), tmpl.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), tmpl.getVersionNeededToExtract());
			assertFalse(tmpl.isEncrypted());
			assertFalse(tmpl.isStrongEncryption());
			assertEquals(getLocalDate("20080928090150"), tmpl.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), tmpl.getDiskNumberStart());
			assertEquals(4, tmpl.getExtraFields().size());
			assertHasExtraField(tmpl, new ExtendedTimestampExtraField(true, getUtcDate("20080928070149"), getUtcDate("20080928070155"), null));
			assertHasExtraField(tmpl, new ExtendedTimestampExtraField(false, getUtcDate("20080928070149"), null, null));
			assertHasExtraField(tmpl, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(tmpl, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(1000)));
			assertEquals("Comment for the absolute shambolic link.", tmpl.getComment());
			assertEquals(new AbsoluteLocation("/tmp"), tmpl.getLinkTarget());

			ZipFileEntry rsf = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(rsf, root.getChildEntries().get("räksmörgås.txt"));
			assertSame(root, rsf.getParent());
			assertSame(ZipVersionMadeBy.UNIX, rsf.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), rsf.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, rsf.getCompressionMethod());
			assertEquals("Contents of räksmörgås\n", Files.readTextFile(rsf, Charsets.UTF8));
			assertEquals(UnsignedInteger.valueOf(0xb3bc2c48L), rsf.getCrc32());
			assertEquals("räksmörgås.txt", rsf.getName());
			assertEquals(UnsignedByte.valueOf(23), rsf.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), rsf.getVersionNeededToExtract());
			assertFalse(rsf.isEncrypted());
			assertFalse(rsf.isStrongEncryption());
			assertFalse(rsf.isCompressedPatchData());
			assertEquals(getLocalDate("20080928081430"), rsf.getLastModified());
			assertEquals(26L, rsf.getCompressedSize().longValue());
			assertEquals(26L, rsf.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), rsf.getDiskNumberStart());
			assertTrue(rsf.isAppearingToBeTextFile());
			assertEquals(4, rsf.getExtraFields().size());
			assertHasExtraField(rsf, new ExtendedTimestampExtraField(true, getUtcDate("20080928061430"), getUtcDate("20080928061446"), null));
			assertHasExtraField(rsf, new ExtendedTimestampExtraField(false, getUtcDate("20080928061430"), null, null));
			assertHasExtraField(rsf, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
			assertHasExtraField(rsf, new NewInfoZipUnixExtraField(UnsignedShort.valueOf(1000), UnsignedShort.valueOf(1000)));
			assertEquals("Räkor, ägg, majonäs, dill.", rsf.getComment());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void test7ZipWindows() throws Exception
	{
		Date nowIsh = new Date();

		ZipFile zf = new ZipFile(getTestDataFile("zip/7ZipWindows.zip"));
		try
		{
			assertEquals(4, zf.size());
			assertEquals("", zf.getComment());

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
			assertTrue(root.getLastModified().getTime() >= nowIsh.getTime());
			assertEquals(UnsignedShort.valueOf(0), root.getDiskNumberStart());
			assertEquals("", root.getComment());

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
			assertEquals("d", d.getName());
			assertEquals(UnsignedByte.valueOf(20), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(getLocalDate("20080928095756"), d.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());

			ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1_stored"));
			assertSame(f1, root.getChildEntries().get("f1_stored"));
			assertSame(root, f1.getParent());
			assertSame(ZipVersionMadeBy.MSDOS, f1.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f1.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f1.getCompressionMethod());
			assertEquals("Contents of f1_stored.", Files.readTextFile(f1));
			assertEquals(UnsignedInteger.valueOf(0x366251a7L), f1.getCrc32());
			assertEquals("f1_stored", f1.getName());
			assertEquals(UnsignedByte.valueOf(20), f1.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f1.getVersionNeededToExtract());
			assertFalse(f1.isEncrypted());
			assertFalse(f1.isStrongEncryption());
			assertFalse(f1.isCompressedPatchData());
			assertEquals(getLocalDate("20080928095736"), f1.getLastModified());
			assertEquals(22L, f1.getCompressedSize().longValue());
			assertEquals(22L, f1.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f1.getDiskNumberStart());
			// 7-Zip seems to ignore this
			assertFalse(f1.isAppearingToBeTextFile());
			assertEquals("", f1.getComment());

			ZipFileEntry f2 = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/f2_deflated"));
			assertSame(f2, d.getChildEntries().get("f2_deflated"));
			assertSame(d, f2.getParent());
			assertSame(ZipVersionMadeBy.MSDOS, f2.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f2.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f2.getCompressionMethod());
			assertEquals("Contents of f2_deflated. Contents of f2_deflated.", Files.readTextFile(f2));
			assertEquals(UnsignedInteger.valueOf(0x5c489caeL), f2.getCrc32());
			assertEquals("f2_deflated", f2.getName());
			assertEquals(UnsignedByte.valueOf(20), f2.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f2.getVersionNeededToExtract());
			assertFalse(f2.isEncrypted());
			assertFalse(f2.isStrongEncryption());
			assertFalse(f2.isCompressedPatchData());
			assertEquals(getLocalDate("20080928095810"), f2.getLastModified());
			assertEquals(41L, f2.getCompressedSize().longValue());
			assertEquals(49L, f2.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f2.getDiskNumberStart());
			// 7-Zip seems to ignore this
			assertFalse(f2.isAppearingToBeTextFile());
			assertEquals("", f2.getComment());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void test7ZipWindowsCharacterEncodingIssues() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/7ZipWindowsWCharacterEncodingIssues.zip"), CP437, CP1252);
		try
		{
			assertEquals(2, zf.size());
			assertEquals("", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
			assertEquals("Contents of räksmörgås.txt\r\nContents of räksmörgås.txt", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0xe6c29712L), f.getCrc32());
			assertEquals("räksmörgås.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getLocalDate("20080928141724"), f.getLastModified());
			assertEquals(47L, f.getCompressedSize().longValue());
			assertEquals(54L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// 7-Zip seems to ignore this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void test7ZipBZip2Windows() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/7ZipBZip2Windows.zip"), CP437, CP1252);
		try
		{
			assertEquals(2, zf.size());
			assertEquals("", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/f.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
			assertSame(BZip2CompressionMethod.INSTANCE, f.getCompressionMethod());
			assertTrue(Files.readTextFile(f, CP1252).contains("This text is likely to be cåmpressed."));
			assertEquals(UnsignedInteger.valueOf(0x8f75bbcfL), f.getCrc32());
			assertEquals("f.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			// This should really have been 46 according to the spec. Seems as if
			// implementors of 7-Zip missed that. 
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getLocalDate("20080928112428"), f.getLastModified());
			assertEquals(120L, f.getCompressedSize().longValue());
			assertEquals(1463L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// 7-Zip seems to ignore this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testWinzipWindows() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/WinZipWindows.zip"), CP437, CP1252);
		try
		{
			assertEquals(2, zf.size());
			assertEquals("", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.MAXIMUM_COMPRESSION, f.getCompressionMethod());
			assertEquals("Contents of räksmörgås.txt\r\nContents of räksmörgås.txt", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0xe6c29712L), f.getCrc32());
			assertEquals("räksmörgås.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getLocalDate("20080928141724"), f.getLastModified());
			assertEquals(33L, f.getCompressedSize().longValue());
			assertEquals(54L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			assertTrue(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(1, f.getExtraFields().size());
			assertHasExtraField(f, new NtfsExtraField(false, new WinNtTime(getUtcDate("20080928121723115")), new WinNtTime(getUtcDate("20080928151027959")), new WinNtTime(getUtcDate("20080928121700115"))));
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testWinzipWindowsUnicode() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/WinZipWindowsUnicode.zip"), CP437, CP1252);
		try
		{
			assertEquals(4, zf.size());
			assertEquals("WinZip archive comment with strange characters in Unicode(?): åäö", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.MAXIMUM_COMPRESSION, f.getCompressionMethod());
			assertEquals("Contents of räksmörgås.txt\r\nContents of räksmörgås.txt", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0xe6c29712L), f.getCrc32());
			assertEquals("räksmörgås.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getLocalDate("20080928141724"), f.getLastModified());
			assertEquals(33L, f.getCompressedSize().longValue());
			assertEquals(54L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			assertTrue(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(3, f.getExtraFields().size());
			assertHasExtraField(f, new NtfsExtraField(false, new WinNtTime(getUtcDate("20080928121723115")), new WinNtTime(getUtcDate("20080928151027959")), new WinNtTime(getUtcDate("20080928121700115"))));
			assertHasExtraField(f, new UnicodePathExtraField(true, new AbsoluteLocation("/räksmörgås.txt"), false));
			assertHasExtraField(f, new UnicodePathExtraField(false, new AbsoluteLocation("/räksmörgås.txt"), false));

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/katalåg"));
			assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
			assertEquals("katalåg", d.getName());
			assertEquals(UnsignedByte.valueOf(20), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(getLocalDate("20080928142142"), d.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());
			assertEquals(3, d.getExtraFields().size());
			assertHasExtraField(d, new NtfsExtraField(false, new WinNtTime(getUtcDate("20080928122140897")), new WinNtTime(getUtcDate("20080928160420506")), new WinNtTime(getUtcDate("20080928122127506"))));
			assertHasExtraField(d, new UnicodePathExtraField(true, new AbsoluteLocation("/katalåg"), true));
			assertHasExtraField(d, new UnicodePathExtraField(false, new AbsoluteLocation("/katalåg"), true));

			ZipFileEntry f2 = (ZipFileEntry) zf.get(new AbsoluteLocation("/katalåg/fil i katalåg.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f2.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f2.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.MAXIMUM_COMPRESSION, f2.getCompressionMethod());
			assertEquals(UnsignedInteger.valueOf(0x366e7245L), f2.getCrc32());
			assertEquals("fil i katalåg.txt", f2.getName());
			assertEquals(UnsignedByte.valueOf(20), f2.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f2.getVersionNeededToExtract());
			assertFalse(f2.isEncrypted());
			assertFalse(f2.isStrongEncryption());
			assertFalse(f2.isCompressedPatchData());
			assertEquals(getLocalDate("20080928142208"), f2.getLastModified());
			assertEquals(48L, f2.getCompressedSize().longValue());
			assertEquals(1071L, f2.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f2.getDiskNumberStart());
			assertTrue(f2.isAppearingToBeTextFile());
			assertEquals("", f2.getComment());
			assertEquals(3, f2.getExtraFields().size());
			assertHasExtraField(f2, new NtfsExtraField(false, new WinNtTime(getUtcDate("20080928122206053")), new WinNtTime(getUtcDate("20080928122206037")), new WinNtTime(getUtcDate("20080928122136975"))));
			assertHasExtraField(f2, new UnicodePathExtraField(true, new AbsoluteLocation("/katalåg/fil i katalåg.txt"), false));
			assertHasExtraField(f2, new UnicodePathExtraField(false, new AbsoluteLocation("/katalåg/fil i katalåg.txt"), false));
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testWinzipWindowsLzma() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/WinZipWindowsLzma.zip"), CP437, CP1252);
		try
		{
			assertEquals(2, zf.size());
			assertEquals("", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
			assertSame(LzmaCompressionMethod.DEFAULT_INSTANCE, f.getCompressionMethod());
			assertEquals("Contents of räksmörgås.txt\r\nContents of räksmörgås.txt", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0xe6c29712L), f.getCrc32());
			assertEquals("räksmörgås.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(63), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(63), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getLocalDate("20080928141724"), f.getLastModified());
			assertEquals(46L, f.getCompressedSize().longValue());
			assertEquals(54L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(1, f.getExtraFields().size());
			assertHasExtraField(f, new NtfsExtraField(false, new WinNtTime(getUtcDate("20080928121723115")), new WinNtTime(getUtcDate("20080928121723115")), new WinNtTime(getUtcDate("20080928121700115"))));
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testMaxOsX() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/MacOSX.zip"), Charsets.UTF8, Charsets.UTF8);
		try
		{
			assertEquals(8, zf.size());
			assertEquals("", zf.getComment());

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/foo"));
			assertSame(ZipVersionMadeBy.UNIX, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, UnixEntityMode.forCode(0755)), d.getExternalFileAttributes());
			assertEquals("foo", d.getName());
			assertEquals(UnsignedByte.valueOf(21), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(getUtcDate("20081216103002"), d.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());
			assertEquals(2, d.getExtraFields().size());
			assertHasExtraField(d, new InfoZipUnixExtraField(getUtcDate("20081216103628"), getUtcDate("20081216103001")));
			assertHasExtraField(d, new InfoZipUnixExtraField(getUtcDate("20081216103628"), getUtcDate("20081216103001"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));

			// Tricky. Mac OS X uses different signs for å, ä and ö. Isn't there a
			// bit too much room in UTF-8? This is enough to drive a man insane...
			assertNull(zf.get(new AbsoluteLocation("/räksmörgås.html")));
			// This å, ä and ö are copy-pasted 
			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/räksmörgås.html"));
			assertSame(ZipVersionMadeBy.UNIX, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
			assertTrue(Files.readTextFile(f, Charsets.UTF8).contains("Contents of räksmörgås.html"));
			assertEquals(UnsignedInteger.valueOf(0x839b4fd9L), f.getCrc32());
			// Again, these å:s, ä:s and ö:s are copy-pasted
			assertEquals("räksmörgås.html", f.getName());
			assertEquals(UnsignedByte.valueOf(21), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216104442"), f.getLastModified());
			assertEquals(330L, f.getCompressedSize().longValue());
			assertEquals(1223L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(2, f.getExtraFields().size());
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104441")));
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104441"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));

			f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
			assertSame(ZipVersionMadeBy.UNIX, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
			assertEquals("Contents of test.txt\nContents of test.txt\n", Files.readTextFile(f, Charsets.UTF8));
			assertEquals(UnsignedInteger.valueOf(0x7144CAACL), f.getCrc32());
			assertEquals("test.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(21), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216104334"), f.getLastModified());
			assertEquals(26L, f.getCompressedSize().longValue());
			assertEquals(42L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(2, f.getExtraFields().size());
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104333")));
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104333"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));

			d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/__MACOSX"));
			assertSame(ZipVersionMadeBy.UNIX, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, UnixEntityMode.forCode(0775)), d.getExternalFileAttributes());
			assertEquals("__MACOSX", d.getName());
			assertEquals(UnsignedByte.valueOf(21), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(getUtcDate("20081216104454"), d.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());
			assertEquals(2, d.getExtraFields().size());
			assertHasExtraField(d, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104453")));
			assertHasExtraField(d, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104453"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));

			f = (ZipFileEntry) zf.get(new AbsoluteLocation("/__MACOSX/._test.txt"));
			assertSame(ZipVersionMadeBy.UNIX, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
			// Java's InflaterInputStream cannot read this		
			//		System.out.println(Files.readTextFile(f, Charsets.UTF8));
			//		assertEquals("Contents of test.txt\nContents of test.txt\n", Files.readTextFile(f, Charsets.UTF8));
			assertEquals(UnsignedInteger.valueOf(0xb75c82beL), f.getCrc32());
			assertEquals("._test.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(21), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216104334"), f.getLastModified());
			assertEquals(53L, f.getCompressedSize().longValue());
			assertEquals(108L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(2, f.getExtraFields().size());
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104333")));
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216104333"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));

			f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt alias"));
			assertSame(ZipVersionMadeBy.UNIX, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
			assertEquals(UnsignedInteger.valueOf(0L), f.getCrc32());
			assertEquals("test.txt alias", f.getName());
			assertEquals(UnsignedByte.valueOf(21), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216103908"), f.getLastModified());
			assertEquals(0L, f.getCompressedSize().longValue());
			assertEquals(0L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(2, f.getExtraFields().size());
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216103908")));
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216103908"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));

			f = (ZipFileEntry) zf.get(new AbsoluteLocation("/__MACOSX/._test.txt alias"));
			assertSame(ZipVersionMadeBy.UNIX, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644)), f.getExternalFileAttributes());
			assertSame(DeflatedCompressionMethod.NORMAL_COMPRESSION, f.getCompressionMethod());
			assertEquals(UnsignedInteger.valueOf(0x701f8d22L), f.getCrc32());
			assertEquals("._test.txt alias", f.getName());
			assertEquals(UnsignedByte.valueOf(21), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216103908"), f.getLastModified());
			assertEquals(9355L, f.getCompressedSize().longValue());
			assertEquals(43010L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(2, f.getExtraFields().size());
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216103908")));
			assertHasExtraField(f, new InfoZipUnixExtraField(getUtcDate("20081216104453"), getUtcDate("20081216103908"), UnsignedShort.valueOf(501), UnsignedShort.valueOf(501)));
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testWindowsWithMsDosAttributes() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/WindowsWithMsDosAttributes.zip"), CP437, CP1252);
		try
		{
			assertEquals(4, zf.size());
			assertEquals("", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
			assertSame(ZipVersionMadeBy.WINDOWS_NTFS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new NtfsExternalFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.HIDDEN, NtfsFileAttributes.ARCHIVE)), f.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
			assertEquals("Contents of test.txt.", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0x15cfdc85L), f.getCrc32());
			assertEquals("test.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216083136"), f.getLastModified());
			assertEquals(21L, f.getCompressedSize().longValue());
			assertEquals(21L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			assertTrue(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(0, f.getExtraFields().size());

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
			assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES, d.getExternalFileAttributes());
			assertEquals("d", d.getName());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());

			f = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/foo.txt"));
			assertSame(ZipVersionMadeBy.WINDOWS_NTFS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new NtfsExternalFileAttributes(EnumSet.of(NtfsFileAttributes.ARCHIVE)), f.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
			assertEquals("", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0), f.getCrc32());
			assertEquals("foo.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081217105150"), f.getLastModified());
			assertEquals(0L, f.getCompressedSize().longValue());
			assertEquals(0L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(0, f.getExtraFields().size());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void test7ZipWithMsDosAttributes() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/7ZipWithMsDosAttributes.zip"), CP437, CP1252);
		try
		{
			assertEquals(4, zf.size());
			assertEquals("", zf.getComment());

			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/test.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new MsDosExternalFileAttributes(MsDosFileAttributes.ARCHIVE, MsDosFileAttributes.HIDDEN_FILE, MsDosFileAttributes.READ_ONLY), f.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
			assertEquals("Contents of test.txt.", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0x15cfdc85L), f.getCrc32());
			assertEquals("test.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081216083136"), f.getLastModified());
			assertEquals(21L, f.getCompressedSize().longValue());
			assertEquals(21L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			// Doesn't get this
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(0, f.getExtraFields().size());

			ZipDirectoryEntry d = (ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d"));
			assertSame(ZipVersionMadeBy.MSDOS, d.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(new MsDosExternalFileAttributes(MsDosFileAttributes.ARCHIVE, MsDosFileAttributes.HIDDEN_FILE, MsDosFileAttributes.SUB_DIRECTORY), d.getExternalFileAttributes());
			assertEquals("d", d.getName());
			assertEquals(UnsignedByte.valueOf(20), d.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), d.getVersionNeededToExtract());
			assertFalse(d.isEncrypted());
			assertFalse(d.isStrongEncryption());
			assertEquals(getUtcDate("20081217105154"), d.getLastModified());
			assertEquals(UnsignedShort.valueOf(0), d.getDiskNumberStart());
			assertEquals("", d.getComment());

			f = (ZipFileEntry) zf.get(new AbsoluteLocation("/d/foo.txt"));
			assertSame(ZipVersionMadeBy.MSDOS, f.getExternalFileAttributes().getVersionMadeBy());
			assertEquals(MsDosExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, f.getExternalFileAttributes());
			assertSame(StoredCompressionMethod.INSTANCE, f.getCompressionMethod());
			assertEquals("", Files.readTextFile(f, CP1252));
			assertEquals(UnsignedInteger.valueOf(0), f.getCrc32());
			assertEquals("foo.txt", f.getName());
			assertEquals(UnsignedByte.valueOf(20), f.getVersionUsedToCreate());
			assertEquals(UnsignedByte.valueOf(10), f.getVersionNeededToExtract());
			assertFalse(f.isEncrypted());
			assertFalse(f.isStrongEncryption());
			assertFalse(f.isCompressedPatchData());
			assertEquals(getUtcDate("20081217105150"), f.getLastModified());
			assertEquals(0L, f.getCompressedSize().longValue());
			assertEquals(0L, f.getUncompressedSize().longValue());
			assertEquals(UnsignedShort.valueOf(0), f.getDiskNumberStart());
			assertFalse(f.isAppearingToBeTextFile());
			assertEquals("", f.getComment());
			assertEquals(0, f.getExtraFields().size());
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testInterleavingReadsFromDifferentFilesInArchive() throws IOException
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/InfoZipSimpleUnix.zip"));
		try
		{
			ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1_stored"));
			ZipFileEntry f2 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f2_deflated"));
			Reader r1 = new InputStreamReader(f1.openForRead());
			try
			{
				Reader r2 = new InputStreamReader(f2.openForRead());
				try
				{
					char[] carr = new char[12];
					r1.read(carr);
					assertEquals("Contents of ", new String(carr));
					r2.read(carr);
					assertEquals("Contents of ", new String(carr));

					carr = new char[2];
					r1.read(carr);
					assertEquals("f1", new String(carr));
					r2.read(carr);
					assertEquals("f2", new String(carr));
				}
				finally
				{
					r2.close();
				}
			}
			finally
			{
				r1.close();
			}
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testInterleavingReadsFromTwoStreamsOpenOnTheSameFileInArchive() throws IOException
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/InfoZipSimpleUnix.zip"));
		try
		{
			ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1_stored"));
			Reader r1 = new InputStreamReader(f1.openForRead());
			try
			{
				Reader r2 = new InputStreamReader(f1.openForRead());
				try
				{
					char[] carr = new char[12];
					r1.read(carr);
					assertEquals("Contents of ", new String(carr));
					r2.read(carr);
					assertEquals("Contents of ", new String(carr));

					carr = new char[2];
					r1.read(carr);
					assertEquals("f1", new String(carr));
					r2.read(carr);
					assertEquals("f1", new String(carr));
				}
				finally
				{
					r2.close();
				}
			}
			finally
			{
				r1.close();
			}
		}
		finally
		{
			zf.close();
		}
	}

	@Test
	public void testReadingFromClosedArchive() throws IOException
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/InfoZipSimpleUnix.zip"));
		try
		{
			ZipFileEntry f1 = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1_stored"));
			Reader r1 = new InputStreamReader(f1.openForRead());
			try
			{
				char[] carr = new char[12];
				r1.read(carr);
				assertEquals("Contents of ", new String(carr));

				zf.close();
				zf = null;

				carr = new char[2];
				try
				{
					r1.read(carr);
				}
				catch (IllegalStateException e)
				{
					// ok
				}
			}
			finally
			{
				r1.close();
			}
		}
		finally
		{
			if (zf != null)
			{
				zf.close();
			}
		}
	}

	@Test
	public void testRandomAccess()
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/InfoZipSimpleUnix.zip"));
		try
		{
			ZipFileEntry f = (ZipFileEntry) zf.get(new AbsoluteLocation("/f1_stored"));
			try
			{
				f.openForRandomAccess(RandomAccessMode.READ_WRITE);
				fail();
			}
			catch (ReadOnlyException e)
			{
				// ok
			}

			RandomAccess ra = f.openForRandomAccess(RandomAccessMode.READ_ONLY);
			try
			{
				assertEquals(15, ra.length());
				assertEquals('C', ra.read());
				byte[] barr = new byte[7];
				assertEquals(7, ra.read(barr));
				assertEquals("ontents", new String(barr));
				ra.seek(12);
				assertEquals(3, ra.read(barr));
				assertEquals("f1\nents", new String(barr));
				ra.seek(12);
				assertEquals(3, ra.read(barr, 2, 5));
				assertEquals("f1f1\nts", new String(barr));
				ra.seek(14);
				assertEquals('\n', ra.read());
				assertEquals(-1, ra.read());
				assertEquals(-1, ra.read(barr));
				assertEquals(-1, ra.read(barr, 2, 2));
				assertEquals(15, ra.getFilePointer());
			}
			finally
			{
				ra.close();
			}

			try
			{
				((ZipFileEntry) zf.get(new AbsoluteLocation("/f2_deflated"))).openForRandomAccess(RandomAccessMode.READ_ONLY);
				fail();
			}
			catch (UnsupportedOperationException e)
			{
				// ok
			}
		}
		finally
		{
			zf.close();
		}
	}
	
	@Test
	public void testZipFileWithEmptyDirectory()
	{
		// A Zip file with an empty root directory
		ZipFile zf = new ZipFile(getTestDataFile("zip/empty_dir.zip"));
		try
		{
			// The empty directory should be ignored, but every Zip file still
			// contains a root directory.
			assertEquals(1, zf.size());
		}
		finally
		{
			zf.close();
		}
	}
	
	@Test
	public void testZipFileWithTooShortExtraInfoEntries()
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/zip_w_too_short_extra_info_entries.zip"));
		try
		{
			assertEquals(3, zf.size());
			assertEquals(2, zf.getRootEntry().getChildEntries().size());
			assertTrue(((ZipDirectoryEntry) zf.get(new AbsoluteLocation("/d1"))).isEmpty());
			assertArrayEquals(new byte[] { 4, 75 }, StreamUtil.readStreamFully(((ZipFileEntry) zf.get(new AbsoluteLocation("/f1"))).openForRead(), 2));
		}
		finally
		{
			zf.close();
		}
	}
	
	@Test
	public void testEntryThatNeedsAnExtraDummyByteToInflateProperly() throws Exception
	{
		ZipFile zf = new ZipFile(getTestDataFile("zip/entry_that_needs_an_extra_dummy_byte_to_inflate_properly.zip"));
		try
		{
			assertEquals("åäö", new String(StreamUtil.readStreamFully(((ZipFileEntry) zf.get(new AbsoluteLocation("/f"))).openForRead(), 512), Charsets.UTF8.name()));
		}
		finally
		{
			zf.close();
		}
	}
}
