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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.at4j.support.nio.charset.Charsets;
import org.entityfs.RandomAccess;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.el.RelativeLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.exception.ReadOnlyException;
import org.entityfs.support.io.RandomAccessMode;
import org.entityfs.util.Files;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class TarFileTest extends AbstractTarFileTest
{
	private void assertHasStandardRootEntry(TarDirectoryEntry root)
	{
		assertNull(root.getParent());
		assertEquals("/", root.getName());
		assertEquals(AbsoluteLocation.ROOT_DIR, root.getLocation());
		assertEquals(0, root.getChecksum());
		assertEquals(UnixEntityMode.forCode(0755), root.getEntityMode());
		assertWithinLast30Seconds(root.getLastModificationTime());
		assertEquals(0, root.getOwnerGid());
		assertEquals(0, root.getOwnerUid());
	}

	@Test
	public void testEmptyFile()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/empty.tar"));
		try
		{
			assertEquals(1, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSingleEmptyFile()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/singleEmptyFile.tar"));
		try
		{
			assertEquals(2, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/empty"));
			assertSame(f, root.getChildEntries().get("empty"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/empty"), f.getLocation());
			assertEquals("empty", f.getName());
			assertEquals(4664, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081222171224"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(512, f.getStartPosOfFileData());
			assertEquals(0, f.getSize());
			assertEquals("", Files.readTextFile(f));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSingleFile()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/singleFile.tar"));
		try
		{
			assertEquals(2, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/test.txt"));
			assertSame(f, root.getChildEntries().get("test.txt"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/test.txt"), f.getLocation());
			assertEquals("test.txt", f.getName());
			assertEquals(4958, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081222092338"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(512, f.getStartPosOfFileData());
			assertEquals(28, f.getSize());
			assertEquals("Contents of test.txt åäö\n", Files.readTextFile(f, Charsets.UTF8));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSingleFileWithStrangeCharactersInName()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/singleFileWithStrangeCharactersInName.tar"), Charsets.UTF8);
		try
		{
			assertEquals(2, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(f, root.getChildEntries().get("räksmörgås.txt"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/räksmörgås.txt"), f.getLocation());
			assertEquals("räksmörgås.txt", f.getName());
			assertEquals(6389, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081222092850"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(512, f.getStartPosOfFileData());
			assertEquals(30, f.getSize());
			assertEquals("Contents of räksmörgås.txt\n", Files.readTextFile(f, Charsets.UTF8));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testFileSpanningSeveral512kBlocks()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/fileSpanningSeveral512kBlocks.tar"));
		try
		{
			assertEquals(3, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/cheese.txt"));
			assertSame(f, root.getChildEntries().get("cheese.txt"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/cheese.txt"), f.getLocation());
			assertEquals("cheese.txt", f.getName());
			assertEquals(5140, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081222173225"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(512, f.getStartPosOfFileData());
			assertEquals(7369, f.getSize());
			assertTrue(Files.readTextFile(getTestDataFile("/the_complete_book_on_cheese.txt")).startsWith(Files.readTextFile(f)));

			f = (UstarFileEntry) tf.get(new AbsoluteLocation("/other_file.txt"));
			assertSame(f, root.getChildEntries().get("other_file.txt"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/other_file.txt"), f.getLocation());
			assertEquals("other_file.txt", f.getName());
			assertEquals(5568, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081222173248"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(8704, f.getStartPosOfFileData());
			assertEquals(23, f.getSize());
			assertEquals("Contents of other file\n", Files.readTextFile(f));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSingleDirectory()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/singleDirectory.tar"));
		try
		{
			assertEquals(2, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(0, d.getChildEntries().size());
			assertEquals(4268, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081222170807"), d.getLastModificationTime());
			assertEquals(26, d.getOwnerGid());
			assertEquals(34, d.getOwnerUid());
			assertEquals("tape", d.getOwnerGroupName());
			assertEquals("backup", d.getOwnerUserName());
			assertEquals("", d.getUstarVersion());
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSomeFilesAndDirectories()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"));
		try
		{
			assertEquals(6, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(4659, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081222171505"), f1.getLastModificationTime());
			assertEquals(1000, f1.getOwnerGid());
			assertEquals(1000, f1.getOwnerUid());
			assertEquals("kalle", f1.getOwnerGroupName());
			assertEquals("kalle", f1.getOwnerUserName());
			assertEquals("", f1.getUstarVersion());
			assertEquals(3072, f1.getStartPosOfFileData());
			assertEquals(19, f1.getSize());
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			UstarDirectoryEntry d1 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d1"));
			assertSame(d1, root.getChildEntries().get("d1"));
			assertSame(root, d1.getParent());
			assertEquals(new AbsoluteLocation("/d1"), d1.getLocation());
			assertEquals("d1", d1.getName());
			assertEquals(1, d1.getChildEntries().size());
			assertEquals(4312, d1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d1.getEntityMode());
			assertEquals(getUtcDate("20081222171515"), d1.getLastModificationTime());
			assertEquals(1000, d1.getOwnerGid());
			assertEquals(1000, d1.getOwnerUid());
			assertEquals("kalle", d1.getOwnerGroupName());
			assertEquals("kalle", d1.getOwnerUserName());
			assertEquals("", d1.getUstarVersion());

			UstarDirectoryEntry d2 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(0, d2.getChildEntries().size());
			assertEquals(4317, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081222171519"), d2.getLastModificationTime());
			assertEquals(1000, d2.getOwnerGid());
			assertEquals(1000, d2.getOwnerUid());
			assertEquals("kalle", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("", d2.getUstarVersion());

			UstarDirectoryEntry d1d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d1/d"));
			assertSame(d1d, d1.getChildEntries().get("d"));
			assertSame(d1, d1d.getParent());
			assertEquals(new AbsoluteLocation("/d1/d"), d1d.getLocation());
			assertEquals("d", d1d.getName());
			assertEquals(1, d1d.getChildEntries().size());
			assertEquals(4459, d1d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d1d.getEntityMode());
			assertEquals(getUtcDate("20081222171536"), d1d.getLastModificationTime());
			assertEquals(1000, d1d.getOwnerGid());
			assertEquals(1000, d1d.getOwnerUid());
			assertEquals("kalle", d1d.getOwnerGroupName());
			assertEquals("kalle", d1d.getOwnerUserName());
			assertEquals("", d1d.getUstarVersion());

			UstarFileEntry f2 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d1/d/f2.txt"));
			assertSame(f2, d1d.getChildEntries().get("f2.txt"));
			assertSame(d1d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d1/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(5006, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081222171536"), f2.getLastModificationTime());
			assertEquals(1000, f2.getOwnerGid());
			assertEquals(1000, f2.getOwnerUid());
			assertEquals("kalle", f2.getOwnerGroupName());
			assertEquals("kalle", f2.getOwnerUserName());
			assertEquals("", f2.getUstarVersion());
			assertEquals(1536, f2.getStartPosOfFileData());
			assertEquals(19, f2.getSize());
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSymbolicLinks()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/symbolicLinks.tar"));
		try
		{
			assertEquals(3, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarSymbolicLinkEntry l1 = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/foo"));
			assertSame(l1, root.getChildEntries().get("foo"));
			assertSame(root, l1.getParent());
			assertEquals(new AbsoluteLocation("/foo"), l1.getLocation());
			assertEquals("foo", l1.getName());
			assertEquals(4806, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l1.getEntityMode());
			assertEquals(getUtcDate("20081223062918"), l1.getLastModificationTime());
			assertEquals(1000, l1.getOwnerGid());
			assertEquals(1000, l1.getOwnerUid());
			assertEquals("kalle", l1.getOwnerGroupName());
			assertEquals("kalle", l1.getOwnerUserName());
			assertEquals("", l1.getUstarVersion());
			assertEquals(new AbsoluteLocation("/foo"), l1.getLinkTarget());

			UstarSymbolicLinkEntry l2 = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/bar"));
			assertSame(l2, root.getChildEntries().get("bar"));
			assertSame(root, l2.getParent());
			assertEquals(new AbsoluteLocation("/bar"), l2.getLocation());
			assertEquals("bar", l2.getName());
			assertEquals(4717, l2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l2.getEntityMode());
			assertEquals(getUtcDate("20081223062920"), l2.getLastModificationTime());
			assertEquals(1000, l2.getOwnerGid());
			assertEquals(1000, l2.getOwnerUid());
			assertEquals("kalle", l2.getOwnerGroupName());
			assertEquals("kalle", l2.getOwnerUserName());
			assertEquals("", l2.getUstarVersion());
			assertEquals(new RelativeLocation("bar"), l2.getLinkTarget());
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testGnuTarFileNameLongerThan100Bytes()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/gnuTarFileNameLongerThan100Bytes.tar"));
		try
		{
			assertEquals(3, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/this_directory_name_is_pretty_long"));
			assertSame(d, root.getChildEntries().get("this_directory_name_is_pretty_long"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/this_directory_name_is_pretty_long"), d.getLocation());
			assertEquals("this_directory_name_is_pretty_long", d.getName());
			assertEquals(1, d.getChildEntries().size());
			assertEquals(7797, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081223073543"), d.getLastModificationTime());
			assertEquals(1000, d.getOwnerGid());
			assertEquals(1000, d.getOwnerUid());
			assertEquals("kalle", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("", d.getUstarVersion());

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation(
					"/this_directory_name_is_pretty_long/but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too"));
			assertSame(f, d.getChildEntries().get("but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too"));
			assertSame(d, f.getParent());
			assertEquals(new AbsoluteLocation("/this_directory_name_is_pretty_long/but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too"), f
					.getLocation());
			assertEquals("but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too", f.getName());
			assertEquals(14660, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081223073543"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(2048, f.getStartPosOfFileData());
			assertEquals(32, f.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testPaxTarFileNameLongerThan100Bytes()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/paxTarFileNameLongerThan100Bytes.tar"));
		try
		{
			assertEquals(3, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			PaxDirectoryEntry d = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/this_directory_name_is_pretty_long"));
			assertSame(d, root.getChildEntries().get("this_directory_name_is_pretty_long"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/this_directory_name_is_pretty_long"), d.getLocation());
			assertEquals("this_directory_name_is_pretty_long", d.getName());
			assertEquals(1, d.getChildEntries().size());
			assertEquals(8501, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081223073543"), d.getLastModificationTime());
			assertEquals(1000, d.getOwnerGid());
			assertEquals(1000, d.getOwnerUid());
			assertEquals("kalle", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("00", d.getUstarVersion());
			assertEquals(3, d.getPaxVariables().size());
			assertEquals("1230017759.007882973", d.getPaxVariables().get("atime"));
			assertEquals("1230017743.359564656", d.getPaxVariables().get("ctime"));
			assertEquals("1230017743.359564656", d.getPaxVariables().get("mtime"));

			PaxFileEntry f = (PaxFileEntry) tf.get(new AbsoluteLocation(
					"/this_directory_name_is_pretty_long/but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too"));
			assertSame(f, d.getChildEntries().get("but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too"));
			assertSame(d, f.getParent());
			assertEquals(new AbsoluteLocation("/this_directory_name_is_pretty_long/but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too"), f
					.getLocation());
			assertEquals("but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too", f.getName());
			assertEquals(15364, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081223073543"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("00", f.getUstarVersion());
			assertEquals(3072, f.getStartPosOfFileData());
			assertEquals(32, f.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f));
			assertEquals(4, f.getPaxVariables().size());
			assertEquals("1230017759.007882973", f.getPaxVariables().get("atime"));
			assertEquals("1230017743.359564656", f.getPaxVariables().get("ctime"));
			assertEquals("1230017743.359564656", f.getPaxVariables().get("mtime"));
			assertEquals("this_directory_name_is_pretty_long/but_its_really_nothing_compared_with_the_very_long_name_of_the_file_whose_name_you_are_reading_right_now_in_this_instant.pretty_long_extension_too", f.getPaxVariables().get(
					"path"));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testGnuTarFileNameEqualToAndLongerThan512Bytes()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/gnuTarFileNameLongerThan512Bytes.tar"));
		try
		{
			assertEquals(4, tf.size());

			String dname = getNumericalName(255);

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/" + dname));
			assertSame(d, root.getChildEntries().get(dname));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/" + dname), d.getLocation());
			assertEquals(dname, d.getName());
			assertEquals(1, d.getChildEntries().size());
			assertEquals(9361, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226080331"), d.getLastModificationTime());
			assertEquals(1000, d.getOwnerGid());
			assertEquals(1000, d.getOwnerUid());
			assertEquals("kalle", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("", d.getUstarVersion());

			UstarDirectoryEntry d2 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/" + dname + "/" + dname));
			assertSame(d2, d.getChildEntries().get(dname));
			assertSame(d, d2.getParent());
			assertEquals(new AbsoluteLocation("/" + dname + "/" + dname), d2.getLocation());
			assertEquals(dname, d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(9363, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226080354"), d2.getLastModificationTime());
			assertEquals(1000, d2.getOwnerGid());
			assertEquals(1000, d2.getOwnerUid());
			assertEquals("kalle", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("", d2.getUstarVersion());

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/" + dname + "/" + dname + "/file_with_long_name.txt"));
			assertSame(f, d2.getChildEntries().get("file_with_long_name.txt"));
			assertSame(d2, f.getParent());
			assertEquals(new AbsoluteLocation("/" + dname + "/" + dname + "/file_with_long_name.txt"), f.getLocation());
			assertEquals("file_with_long_name.txt", f.getName());
			assertEquals(9359, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081226080354"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(5632, f.getStartPosOfFileData());
			assertEquals(32, f.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testPaxTarFileNameEqualToAndLongerThan512Bytes()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/paxTarFileNameLongerThan512Bytes.tar"));
		try
		{
			assertEquals(4, tf.size());

			String dname = getNumericalName(255);

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			PaxDirectoryEntry d = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/" + dname));
			assertSame(d, root.getChildEntries().get(dname));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/" + dname), d.getLocation());
			assertEquals(dname, d.getName());
			assertEquals(1, d.getChildEntries().size());
			assertEquals(10065, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226080331"), d.getLastModificationTime());
			assertEquals(1000, d.getOwnerGid());
			assertEquals(1000, d.getOwnerUid());
			assertEquals("kalle", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("00", d.getUstarVersion());
			assertEquals(4, d.getPaxVariables().size());
			assertEquals("1230278613.400801236", d.getPaxVariables().get("atime"));
			assertEquals("1230278611.226795648", d.getPaxVariables().get("ctime"));
			assertEquals("1230278611.226795648", d.getPaxVariables().get("mtime"));
			assertEquals(dname + "/", d.getPaxVariables().get("path"));

			PaxDirectoryEntry d2 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/" + dname + "/" + dname));
			assertSame(d2, d.getChildEntries().get(dname));
			assertSame(d, d2.getParent());
			assertEquals(new AbsoluteLocation("/" + dname + "/" + dname), d2.getLocation());
			assertEquals(dname, d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(10067, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226080354"), d2.getLastModificationTime());
			assertEquals(1000, d2.getOwnerGid());
			assertEquals(1000, d2.getOwnerUid());
			assertEquals("kalle", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("00", d2.getUstarVersion());
			assertEquals("1230278637.665384813", d2.getPaxVariables().get("atime"));
			assertEquals("1230278634.70768915", d2.getPaxVariables().get("ctime"));
			assertEquals("1230278634.70768915", d2.getPaxVariables().get("mtime"));
			assertEquals(dname + "/" + dname + "/", d2.getPaxVariables().get("path"));

			PaxFileEntry f = (PaxFileEntry) tf.get(new AbsoluteLocation("/" + dname + "/" + dname + "/file_with_long_name.txt"));
			assertSame(f, d2.getChildEntries().get("file_with_long_name.txt"));
			assertSame(d2, f.getParent());
			assertEquals(new AbsoluteLocation("/" + dname + "/" + dname + "/file_with_long_name.txt"), f.getLocation());
			assertEquals("file_with_long_name.txt", f.getName());
			assertEquals(10063, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081226080354"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("00", f.getUstarVersion());
			assertEquals(5632, f.getStartPosOfFileData());
			assertEquals(32, f.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f));
			assertEquals("1230278638.606506799", f.getPaxVariables().get("atime"));
			assertEquals("1230278634.70768915", f.getPaxVariables().get("ctime"));
			assertEquals("1230278634.70768915", f.getPaxVariables().get("mtime"));
			assertEquals(dname + "/" + dname + "/file_with_long_name.txt", f.getPaxVariables().get("path"));
			assertEquals("Contents of file with long name\n", Files.readTextFile(f));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testGnuTarLinkTargetLongerThan155Bytes()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/gnuTarLinkTargetLongerThan155Bytes.tar"));
		try
		{
			assertEquals(6, tf.size());

			String s99 = getNumericalName(99);
			String s100 = getNumericalName(100);

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d100 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/" + s100));
			assertSame(d100, root.getChildEntries().get(s100));
			assertSame(root, d100.getParent());
			assertEquals(new AbsoluteLocation("/" + s100), d100.getLocation());
			assertEquals(s100, d100.getName());
			assertEquals(2, d100.getChildEntries().size());
			assertEquals(9359, d100.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d100.getEntityMode());
			assertEquals(getUtcDate("20081230130049"), d100.getLastModificationTime());
			assertEquals(1000, d100.getOwnerGid());
			assertEquals(1000, d100.getOwnerUid());
			assertEquals("kalle", d100.getOwnerGroupName());
			assertEquals("kalle", d100.getOwnerUserName());
			assertEquals("", d100.getUstarVersion());

			UstarFileEntry d100f100 = (UstarFileEntry) tf.get(new AbsoluteLocation("/" + s100 + "/" + s100 + ".txt"));
			assertSame(d100f100, d100.getChildEntries().get(s100 + ".txt"));
			assertSame(d100, d100f100.getParent());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s100 + ".txt"), d100f100.getLocation());
			assertEquals(s100 + ".txt", d100f100.getName());
			assertEquals(9364, d100f100.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), d100f100.getEntityMode());
			assertEquals(getUtcDate("20081230124853"), d100f100.getLastModificationTime());
			assertEquals(1000, d100f100.getOwnerGid());
			assertEquals(1000, d100f100.getOwnerUid());
			assertEquals("kalle", d100f100.getOwnerGroupName());
			assertEquals("kalle", d100f100.getOwnerUserName());
			assertEquals("", d100f100.getUstarVersion());
			assertEquals(3072, d100f100.getStartPosOfFileData());
			assertEquals(22, d100f100.getSize());
			assertEquals("Contents of long file\n", Files.readTextFile(d100f100));

			UstarSymbolicLinkEntry rl = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/" + s100 + ".txt"));
			assertSame(rl, root.getChildEntries().get(s100 + ".txt"));
			assertSame(root, rl.getParent());
			assertEquals(new AbsoluteLocation("/" + s100 + ".txt"), rl.getLocation());
			assertEquals(s100 + ".txt", rl.getName());
			assertEquals(14609, rl.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), rl.getEntityMode());
			assertEquals(getUtcDate("20081230124908"), rl.getLastModificationTime());
			assertEquals(1000, rl.getOwnerGid());
			assertEquals(1000, rl.getOwnerUid());
			assertEquals("kalle", rl.getOwnerGroupName());
			assertEquals("kalle", rl.getOwnerUserName());
			assertEquals("", rl.getUstarVersion());
			assertEquals(new RelativeLocation(s100 + "/" + s100 + ".txt"), rl.getLinkTarget());

			UstarSymbolicLinkEntry al = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/" + s99 + ".txt"));
			assertSame(al, root.getChildEntries().get(s99 + ".txt"));
			assertSame(root, al.getParent());
			assertEquals(new AbsoluteLocation("/" + s99 + ".txt"), al.getLocation());
			assertEquals(s99 + ".txt", al.getName());
			assertEquals(14589, al.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), al.getEntityMode());
			assertEquals(getUtcDate("20081230124923"), al.getLastModificationTime());
			assertEquals(1000, al.getOwnerGid());
			assertEquals(1000, al.getOwnerUid());
			assertEquals("kalle", al.getOwnerGroupName());
			assertEquals("kalle", al.getOwnerUserName());
			assertEquals("", al.getUstarVersion());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"), al.getLinkTarget());

			// This has both a too long file name and a too long link name
			UstarSymbolicLinkEntry d100al = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"));
			assertSame(d100al, d100.getChildEntries().get(s99 + ".txt"));
			assertSame(d100, d100al.getParent());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"), d100al.getLocation());
			assertEquals(s99 + ".txt", d100al.getName());
			assertEquals(14600, d100al.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), d100al.getEntityMode());
			assertEquals(getUtcDate("20081230130049"), d100al.getLastModificationTime());
			assertEquals(1000, d100al.getOwnerGid());
			assertEquals(1000, d100al.getOwnerUid());
			assertEquals("kalle", d100al.getOwnerGroupName());
			assertEquals("kalle", d100al.getOwnerUserName());
			assertEquals("", d100al.getUstarVersion());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"), d100al.getLinkTarget());
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testPaxTarLinkTargetLongerThan155Bytes()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/paxTarLinkTargetLongerThan155Bytes.tar"));
		try
		{
			assertEquals(6, tf.size());

			String s99 = getNumericalName(99);
			String s100 = getNumericalName(100);

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			PaxDirectoryEntry d100 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/" + s100));
			assertSame(d100, root.getChildEntries().get(s100));
			assertSame(root, d100.getParent());
			assertEquals(new AbsoluteLocation("/" + s100), d100.getLocation());
			assertEquals(s100, d100.getName());
			assertEquals(2, d100.getChildEntries().size());
			assertEquals(10063, d100.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d100.getEntityMode());
			assertEquals(getUtcDate("20081230130049"), d100.getLastModificationTime());
			assertEquals(1000, d100.getOwnerGid());
			assertEquals(1000, d100.getOwnerUid());
			assertEquals("kalle", d100.getOwnerGroupName());
			assertEquals("kalle", d100.getOwnerUserName());
			assertEquals("00", d100.getUstarVersion());

			PaxFileEntry d100f100 = (PaxFileEntry) tf.get(new AbsoluteLocation("/" + s100 + "/" + s100 + ".txt"));
			assertSame(d100f100, d100.getChildEntries().get(s100 + ".txt"));
			assertSame(d100, d100f100.getParent());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s100 + ".txt"), d100f100.getLocation());
			assertEquals(s100 + ".txt", d100f100.getName());
			assertEquals(10068, d100f100.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), d100f100.getEntityMode());
			assertEquals(getUtcDate("20081230124853"), d100f100.getLastModificationTime());
			assertEquals(1000, d100f100.getOwnerGid());
			assertEquals(1000, d100f100.getOwnerUid());
			assertEquals("kalle", d100f100.getOwnerGroupName());
			assertEquals("kalle", d100f100.getOwnerUserName());
			assertEquals("00", d100f100.getUstarVersion());
			assertEquals(3072, d100f100.getStartPosOfFileData());
			assertEquals(22, d100f100.getSize());
			assertEquals("Contents of long file\n", Files.readTextFile(d100f100));

			PaxSymbolicLinkEntry rl = (PaxSymbolicLinkEntry) tf.get(new AbsoluteLocation("/" + s100 + ".txt"));
			assertSame(rl, root.getChildEntries().get(s100 + ".txt"));
			assertSame(root, rl.getParent());
			assertEquals(new AbsoluteLocation("/" + s100 + ".txt"), rl.getLocation());
			assertEquals(s100 + ".txt", rl.getName());
			assertEquals(15313, rl.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), rl.getEntityMode());
			assertEquals(getUtcDate("20081230124908"), rl.getLastModificationTime());
			assertEquals(1000, rl.getOwnerGid());
			assertEquals(1000, rl.getOwnerUid());
			assertEquals("kalle", rl.getOwnerGroupName());
			assertEquals("kalle", rl.getOwnerUserName());
			assertEquals("00", rl.getUstarVersion());
			assertEquals(new RelativeLocation(s100 + "/" + s100 + ".txt"), rl.getLinkTarget());

			PaxSymbolicLinkEntry al = (PaxSymbolicLinkEntry) tf.get(new AbsoluteLocation("/" + s99 + ".txt"));
			assertSame(al, root.getChildEntries().get(s99 + ".txt"));
			assertSame(root, al.getParent());
			assertEquals(new AbsoluteLocation("/" + s99 + ".txt"), al.getLocation());
			assertEquals(s99 + ".txt", al.getName());
			assertEquals(15293, al.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), al.getEntityMode());
			assertEquals(getUtcDate("20081230124923"), al.getLastModificationTime());
			assertEquals(1000, al.getOwnerGid());
			assertEquals(1000, al.getOwnerUid());
			assertEquals("kalle", al.getOwnerGroupName());
			assertEquals("kalle", al.getOwnerUserName());
			assertEquals("00", al.getUstarVersion());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"), al.getLinkTarget());

			// This has both a too long file name and a too long link name
			PaxSymbolicLinkEntry d100al = (PaxSymbolicLinkEntry) tf.get(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"));
			assertSame(d100al, d100.getChildEntries().get(s99 + ".txt"));
			assertSame(d100, d100al.getParent());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"), d100al.getLocation());
			assertEquals(s99 + ".txt", d100al.getName());
			assertEquals(15304, d100al.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), d100al.getEntityMode());
			assertEquals(getUtcDate("20081230130049"), d100al.getLastModificationTime());
			assertEquals(1000, d100al.getOwnerGid());
			assertEquals(1000, d100al.getOwnerUid());
			assertEquals("kalle", d100al.getOwnerGroupName());
			assertEquals("kalle", d100al.getOwnerUserName());
			assertEquals("00", d100al.getUstarVersion());
			assertEquals(new AbsoluteLocation("/" + s100 + "/" + s99 + ".txt"), d100al.getLinkTarget());
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testOldTarFormat()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/v7FilesAndDirectories.tar"));
		try
		{
			assertEquals(9, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			TarDirectoryEntry d = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(TarDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(3, d.getChildEntries().size());
			assertEquals(3262, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226142856"), d.getLastModificationTime());
			assertEquals(1000, d.getOwnerGid());
			assertEquals(1000, d.getOwnerUid());

			TarDirectoryEntry d2 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(TarDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(3314, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226141800"), d2.getLastModificationTime());
			assertEquals(1000, d2.getOwnerGid());
			assertEquals(1000, d2.getOwnerUid());

			TarDirectoryEntry d3 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(TarDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(3512, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
			assertEquals(getUtcDate("20081226141800"), d3.getLastModificationTime());
			assertEquals(1000, d3.getOwnerGid());
			assertEquals(1000, d3.getOwnerUid());

			TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(TarFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(3618, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081226141042"), f1.getLastModificationTime());
			assertEquals(1000, f1.getOwnerGid());
			assertEquals(1000, f1.getOwnerUid());
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			TarFileEntry f2 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(TarFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(3770, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081226141053"), f2.getLastModificationTime());
			assertEquals(1000, f2.getOwnerGid());
			assertEquals(1000, f2.getOwnerUid());
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));

			TarSymbolicLinkEntry l1 = (TarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/d/f1.txt"));
			assertSame(TarSymbolicLinkEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt"), l1.getLocation());
			assertEquals("f1.txt", l1.getName());
			assertEquals(4500, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l1.getEntityMode());
			assertEquals(getUtcDate("20081226142856"), l1.getLastModificationTime());
			assertEquals(1000, l1.getOwnerGid());
			assertEquals(1000, l1.getOwnerUid());
			assertEquals(new RelativeLocation("../f1.txt"), l1.getLinkTarget());

			String name99 = getNumericalName(99);

			TarFileEntry f10 = (TarFileEntry) tf.get(new AbsoluteLocation("/" + name99));
			assertSame(TarFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name99));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name99), f10.getLocation());
			assertEquals(name99, f10.getName());
			assertEquals(8257, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
			assertEquals(getUtcDate("20081226141347"), f10.getLastModificationTime());
			assertEquals(1000, f10.getOwnerGid());
			assertEquals(1000, f10.getOwnerUid());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f10));

			String name97 = getNumericalName(97);

			TarFileEntry f11 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/" + name97));
			assertSame(TarFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name97));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name97), f11.getLocation());
			assertEquals(name97, f11.getName());
			assertEquals(8296, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f11.getEntityMode());
			assertEquals(getUtcDate("20081226141140"), f11.getLastModificationTime());
			assertEquals(1000, f11.getOwnerGid());
			assertEquals(1000, f11.getOwnerUid());
			assertEquals("Contents of file with too long name\n", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSolarisDefault()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/solarisDefault.tar"));
		try
		{
			assertEquals(9, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(UstarDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(3, d.getChildEntries().size());
			assertEquals(4986, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226145217"), d.getLastModificationTime());
			assertEquals(1, d.getOwnerGid());
			assertEquals(100, d.getOwnerUid());
			assertEquals("other", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("00", d.getUstarVersion());

			UstarDirectoryEntry d2 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(UstarDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(5039, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226145124"), d2.getLastModificationTime());
			assertEquals(1, d2.getOwnerGid());
			assertEquals(100, d2.getOwnerUid());
			assertEquals("other", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("00", d2.getUstarVersion());

			UstarDirectoryEntry d3 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(UstarDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(5237, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
			assertEquals(getUtcDate("20081226145124"), d3.getLastModificationTime());
			assertEquals(1, d3.getOwnerGid());
			assertEquals(100, d3.getOwnerUid());
			assertEquals("other", d3.getOwnerGroupName());
			assertEquals("kalle", d3.getOwnerUserName());
			assertEquals("00", d3.getUstarVersion());

			UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(UstarFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(5389, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081226145118"), f1.getLastModificationTime());
			assertEquals(1, f1.getOwnerGid());
			assertEquals(100, f1.getOwnerUid());
			assertEquals("other", f1.getOwnerGroupName());
			assertEquals("kalle", f1.getOwnerUserName());
			assertEquals("00", f1.getUstarVersion());
			assertEquals(5632, f1.getStartPosOfFileData());
			assertEquals(19, f1.getSize());
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			UstarFileEntry f2 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(UstarFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(5535, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081226145137"), f2.getLastModificationTime());
			assertEquals(1, f2.getOwnerGid());
			assertEquals(100, f2.getOwnerUid());
			assertEquals("other", f2.getOwnerGroupName());
			assertEquals("kalle", f2.getOwnerUserName());
			assertEquals("00", f2.getUstarVersion());
			assertEquals(2048, f2.getStartPosOfFileData());
			assertEquals(19, f2.getSize());
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));

			UstarSymbolicLinkEntry l1 = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/d/f1.txt"));
			assertSame(UstarSymbolicLinkEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt"), l1.getLocation());
			assertEquals("f1.txt", l1.getName());
			assertEquals(6224, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l1.getEntityMode());
			assertEquals(getUtcDate("20081226145217"), l1.getLastModificationTime());
			assertEquals(1, l1.getOwnerGid());
			assertEquals(100, l1.getOwnerUid());
			assertEquals("other", l1.getOwnerGroupName());
			assertEquals("kalle", l1.getOwnerUserName());
			assertEquals("00", l1.getUstarVersion());
			assertEquals(new RelativeLocation("../f1.txt"), l1.getLinkTarget());

			String name100 = getNumericalName(100);

			UstarFileEntry f10 = (UstarFileEntry) tf.get(new AbsoluteLocation("/" + name100));
			assertSame(UstarFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name100));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name100), f10.getLocation());
			assertEquals(name100, f10.getName());
			assertEquals(10082, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
			assertEquals(getUtcDate("20081226145200"), f10.getLastModificationTime());
			assertEquals(1, f10.getOwnerGid());
			assertEquals(100, f10.getOwnerUid());
			assertEquals("other", f10.getOwnerGroupName());
			assertEquals("kalle", f10.getOwnerUserName());
			assertEquals("00", f10.getUstarVersion());
			assertEquals(512, f10.getStartPosOfFileData());
			assertEquals(32, f10.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f10));

			UstarFileEntry f11 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/" + name100));
			assertSame(UstarFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name100));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name100), f11.getLocation());
			assertEquals(name100, f11.getName());
			assertEquals(10187, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f11.getEntityMode());
			assertEquals(getUtcDate("20081226145205"), f11.getLastModificationTime());
			assertEquals(1, f11.getOwnerGid());
			assertEquals(100, f11.getOwnerUid());
			assertEquals("other", f11.getOwnerGroupName());
			assertEquals("kalle", f11.getOwnerUserName());
			assertEquals("00", f11.getUstarVersion());
			assertEquals(3072, f11.getStartPosOfFileData());
			assertEquals(32, f11.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSolarisNoTrailingSlashesOnDirs()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/solarisNoTrailingSlashesOnDirs.tar"));
		try
		{
			assertEquals(9, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(UstarDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(3, d.getChildEntries().size());
			assertEquals(4939, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226145217"), d.getLastModificationTime());
			assertEquals(1, d.getOwnerGid());
			assertEquals(100, d.getOwnerUid());
			assertEquals("other", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("00", d.getUstarVersion());

			UstarDirectoryEntry d2 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(UstarDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(4992, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226145124"), d2.getLastModificationTime());
			assertEquals(1, d2.getOwnerGid());
			assertEquals(100, d2.getOwnerUid());
			assertEquals("other", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("00", d2.getUstarVersion());

			UstarDirectoryEntry d3 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(UstarDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(5190, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
			assertEquals(getUtcDate("20081226145124"), d3.getLastModificationTime());
			assertEquals(1, d3.getOwnerGid());
			assertEquals(100, d3.getOwnerUid());
			assertEquals("other", d3.getOwnerGroupName());
			assertEquals("kalle", d3.getOwnerUserName());
			assertEquals("00", d3.getUstarVersion());

			UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(UstarFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(5389, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081226145118"), f1.getLastModificationTime());
			assertEquals(1, f1.getOwnerGid());
			assertEquals(100, f1.getOwnerUid());
			assertEquals("other", f1.getOwnerGroupName());
			assertEquals("kalle", f1.getOwnerUserName());
			assertEquals("00", f1.getUstarVersion());
			assertEquals(5632, f1.getStartPosOfFileData());
			assertEquals(19, f1.getSize());
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			UstarFileEntry f2 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(UstarFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(5535, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081226145137"), f2.getLastModificationTime());
			assertEquals(1, f2.getOwnerGid());
			assertEquals(100, f2.getOwnerUid());
			assertEquals("other", f2.getOwnerGroupName());
			assertEquals("kalle", f2.getOwnerUserName());
			assertEquals("00", f2.getUstarVersion());
			assertEquals(2048, f2.getStartPosOfFileData());
			assertEquals(19, f2.getSize());
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));

			UstarSymbolicLinkEntry l1 = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/d/f1.txt"));
			assertSame(UstarSymbolicLinkEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt"), l1.getLocation());
			assertEquals("f1.txt", l1.getName());
			assertEquals(6224, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l1.getEntityMode());
			assertEquals(getUtcDate("20081226145217"), l1.getLastModificationTime());
			assertEquals(1, l1.getOwnerGid());
			assertEquals(100, l1.getOwnerUid());
			assertEquals("other", l1.getOwnerGroupName());
			assertEquals("kalle", l1.getOwnerUserName());
			assertEquals("00", l1.getUstarVersion());
			assertEquals(new RelativeLocation("../f1.txt"), l1.getLinkTarget());

			String name100 = getNumericalName(100);

			UstarFileEntry f10 = (UstarFileEntry) tf.get(new AbsoluteLocation("/" + name100));
			assertSame(UstarFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name100));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name100), f10.getLocation());
			assertEquals(name100, f10.getName());
			assertEquals(10082, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
			assertEquals(getUtcDate("20081226145200"), f10.getLastModificationTime());
			assertEquals(1, f10.getOwnerGid());
			assertEquals(100, f10.getOwnerUid());
			assertEquals("other", f10.getOwnerGroupName());
			assertEquals("kalle", f10.getOwnerUserName());
			assertEquals("00", f10.getUstarVersion());
			assertEquals(512, f10.getStartPosOfFileData());
			assertEquals(32, f10.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f10));

			UstarFileEntry f11 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/" + name100));
			assertSame(UstarFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name100));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name100), f11.getLocation());
			assertEquals(name100, f11.getName());
			assertEquals(10187, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f11.getEntityMode());
			assertEquals(getUtcDate("20081226145205"), f11.getLastModificationTime());
			assertEquals(1, f11.getOwnerGid());
			assertEquals(100, f11.getOwnerUid());
			assertEquals("other", f11.getOwnerGroupName());
			assertEquals("kalle", f11.getOwnerUserName());
			assertEquals("00", f11.getUstarVersion());
			assertEquals(3072, f11.getStartPosOfFileData());
			assertEquals(32, f11.getSize());
			assertEquals("Contents of file with long name\n", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testSolarisExtendedHeaders()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/solarisExtendedHeaders.tar"));
		try
		{
			assertEquals(9, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			PaxDirectoryEntry d = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(PaxDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(3, d.getChildEntries().size());
			assertEquals(4939, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226145217"), d.getLastModificationTime());
			assertEquals(1, d.getOwnerGid());
			assertEquals(100, d.getOwnerUid());
			assertEquals("other", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("00", d.getUstarVersion());
			assertEquals(1, d.getPaxVariables().size());
			assertEquals("1230303137.949289855", d.getPaxVariables().get("mtime"));

			PaxDirectoryEntry d2 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(PaxDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(4992, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226145124"), d2.getLastModificationTime());
			assertEquals(1, d2.getOwnerGid());
			assertEquals(100, d2.getOwnerUid());
			assertEquals("other", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("00", d2.getUstarVersion());
			assertEquals(1, d2.getPaxVariables().size());
			assertEquals("1230303084.152664616", d2.getPaxVariables().get("mtime"));

			PaxDirectoryEntry d3 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(PaxDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(5190, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
			assertEquals(getUtcDate("20081226145124"), d3.getLastModificationTime());
			assertEquals(1, d3.getOwnerGid());
			assertEquals(100, d3.getOwnerUid());
			assertEquals("other", d3.getOwnerGroupName());
			assertEquals("kalle", d3.getOwnerUserName());
			assertEquals("00", d3.getUstarVersion());
			assertEquals(1, d3.getPaxVariables().size());
			assertEquals("1230303084.152663616", d3.getPaxVariables().get("mtime"));

			PaxFileEntry f1 = (PaxFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(PaxFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(5389, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081226145118"), f1.getLastModificationTime());
			assertEquals(1, f1.getOwnerGid());
			assertEquals(100, f1.getOwnerUid());
			assertEquals("other", f1.getOwnerGroupName());
			assertEquals("kalle", f1.getOwnerUserName());
			assertEquals("00", f1.getUstarVersion());
			assertEquals(13824, f1.getStartPosOfFileData());
			assertEquals(19, f1.getSize());
			assertEquals(1, f1.getPaxVariables().size());
			assertEquals("1230303078.100289716", f1.getPaxVariables().get("mtime"));
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			PaxFileEntry f2 = (PaxFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(PaxFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(5535, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081226145137"), f2.getLastModificationTime());
			assertEquals(1, f2.getOwnerGid());
			assertEquals(100, f2.getOwnerUid());
			assertEquals("other", f2.getOwnerGroupName());
			assertEquals("kalle", f2.getOwnerUserName());
			assertEquals("00", f2.getUstarVersion());
			assertEquals(5120, f2.getStartPosOfFileData());
			assertEquals(19, f2.getSize());
			assertEquals(1, f2.getPaxVariables().size());
			assertEquals("1230303097.827946458", f2.getPaxVariables().get("mtime"));
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));

			PaxSymbolicLinkEntry l1 = (PaxSymbolicLinkEntry) tf.get(new AbsoluteLocation("/d/f1.txt"));
			assertSame(PaxSymbolicLinkEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt"), l1.getLocation());
			assertEquals("f1.txt", l1.getName());
			assertEquals(6224, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l1.getEntityMode());
			assertEquals(getUtcDate("20081226145217"), l1.getLastModificationTime());
			assertEquals(1, l1.getOwnerGid());
			assertEquals(100, l1.getOwnerUid());
			assertEquals("other", l1.getOwnerGroupName());
			assertEquals("kalle", l1.getOwnerUserName());
			assertEquals("00", l1.getUstarVersion());
			assertEquals(1, l1.getPaxVariables().size());
			assertEquals("1230303137.949283087", l1.getPaxVariables().get("mtime"));
			assertEquals(new RelativeLocation("../f1.txt"), l1.getLinkTarget());

			String name100 = getNumericalName(100);

			PaxFileEntry f10 = (PaxFileEntry) tf.get(new AbsoluteLocation("/" + name100));
			assertSame(PaxFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name100));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name100), f10.getLocation());
			assertEquals(name100, f10.getName());
			assertEquals(10082, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
			assertEquals(getUtcDate("20081226145200"), f10.getLastModificationTime());
			assertEquals(1, f10.getOwnerGid());
			assertEquals(100, f10.getOwnerUid());
			assertEquals("other", f10.getOwnerGroupName());
			assertEquals("kalle", f10.getOwnerUserName());
			assertEquals("00", f10.getUstarVersion());
			assertEquals(1536, f10.getStartPosOfFileData());
			assertEquals(32, f10.getSize());
			assertEquals(1, f10.getPaxVariables().size());
			assertEquals("1230303120.432890179", f10.getPaxVariables().get("mtime"));
			assertEquals("Contents of file with long name\n", Files.readTextFile(f10));

			PaxFileEntry f11 = (PaxFileEntry) tf.get(new AbsoluteLocation("/d/" + name100));
			assertSame(PaxFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name100));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name100), f11.getLocation());
			assertEquals(name100, f11.getName());
			assertEquals(10187, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f11.getEntityMode());
			assertEquals(getUtcDate("20081226145205"), f11.getLastModificationTime());
			assertEquals(1, f11.getOwnerGid());
			assertEquals(100, f11.getOwnerUid());
			assertEquals("other", f11.getOwnerGroupName());
			assertEquals("kalle", f11.getOwnerUserName());
			assertEquals("00", f11.getUstarVersion());
			assertEquals(7168, f11.getStartPosOfFileData());
			assertEquals(32, f11.getSize());
			assertEquals(1, f11.getPaxVariables().size());
			assertEquals("1230303125.708754787", f11.getPaxVariables().get("mtime"));
			assertEquals("Contents of file with long name\n", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void test7ZipWindows()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/7ZipWindows.tar"), CP437);
		try
		{
			assertEquals(10, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			TarDirectoryEntry d = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(TarDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(3, d.getChildEntries().size());
			assertEquals(2224, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), d.getEntityMode());
			assertEquals(getUtcDate("20081226154604"), d.getLastModificationTime());
			assertEquals(0, d.getOwnerGid());
			assertEquals(0, d.getOwnerUid());

			TarDirectoryEntry d2 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(TarDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(2278, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), d2.getEntityMode());
			assertEquals(getUtcDate("20081226154437"), d2.getLastModificationTime());
			assertEquals(0, d2.getOwnerGid());
			assertEquals(0, d2.getOwnerUid());

			TarDirectoryEntry d3 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(TarDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(2475, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), d3.getEntityMode());
			assertEquals(getUtcDate("20081226154436"), d3.getLastModificationTime());
			assertEquals(0, d3.getOwnerGid());
			assertEquals(0, d3.getOwnerUid());

			TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(TarFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(2644, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), f1.getEntityMode());
			assertEquals(getUtcDate("20081226154354"), f1.getLastModificationTime());
			assertEquals(0, f1.getOwnerGid());
			assertEquals(0, f1.getOwnerUid());
			assertEquals(8704, f1.getStartPosOfFileData());
			assertEquals(18, f1.getSize());
			assertEquals("Contents of f1.txt", Files.readTextFile(f1));

			TarFileEntry fr = (TarFileEntry) tf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(TarFileEntry.class, fr.getClass());
			assertSame(fr, root.getChildEntries().get("räksmörgås.txt"));
			assertSame(root, fr.getParent());
			assertEquals(new AbsoluteLocation("/räksmörgås.txt"), fr.getLocation());
			assertEquals("räksmörgås.txt", fr.getName());
			assertEquals(3683, fr.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), fr.getEntityMode());
			assertEquals(getUtcDate("20081226154413"), fr.getLastModificationTime());
			assertEquals(0, fr.getOwnerGid());
			assertEquals(0, fr.getOwnerUid());
			assertEquals(9728, fr.getStartPosOfFileData());
			assertEquals(26, fr.getSize());
			assertEquals("Contents of räksmörgås.txt", Files.readTextFile(fr, CP1252));

			TarFileEntry f2 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(TarFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(2786, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), f2.getEntityMode());
			assertEquals(getUtcDate("20081226154540"), f2.getLastModificationTime());
			assertEquals(0, f2.getOwnerGid());
			assertEquals(0, f2.getOwnerUid());
			assertEquals(7680, f2.getStartPosOfFileData());
			assertEquals(18, f2.getSize());
			assertEquals("Contents of f2.txt", Files.readTextFile(f2));

			TarFileEntry l1 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/f1.txt.lnk"));
			assertSame(TarFileEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt.lnk"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt.lnk"), l1.getLocation());
			assertEquals("f1.txt.lnk", l1.getName());
			assertEquals(3185, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), l1.getEntityMode());
			assertEquals(getUtcDate("20081226154520"), l1.getLastModificationTime());
			assertEquals(0, l1.getOwnerGid());
			assertEquals(0, l1.getOwnerUid());
			assertEquals(546, l1.getSize());

			String name100 = getNumericalName(100);

			TarFileEntry f10 = (TarFileEntry) tf.get(new AbsoluteLocation("/" + name100));
			assertSame(TarFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name100));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name100), f10.getLocation());
			assertEquals(name100, f10.getName());
			assertEquals(7289, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), f10.getEntityMode());
			assertEquals(getUtcDate("20081226154623"), f10.getLastModificationTime());
			assertEquals(0, f10.getOwnerGid());
			assertEquals(0, f10.getOwnerUid());
			assertEquals(1536, f10.getStartPosOfFileData());
			assertEquals(31, f10.getSize());
			assertEquals("Contents of file with long name", Files.readTextFile(f10));

			TarFileEntry f11 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/" + name100));
			assertSame(TarFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name100));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name100), f11.getLocation());
			assertEquals(name100, f11.getName());
			assertEquals(7325, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0777), f11.getEntityMode());
			assertEquals(getUtcDate("20081226154623"), f11.getLastModificationTime());
			assertEquals(0, f11.getOwnerGid());
			assertEquals(0, f11.getOwnerUid());
			assertEquals(5120, f11.getStartPosOfFileData());
			assertEquals(31, f11.getSize());
			assertEquals("Contents of file with long name", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testMacDefault()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/macDefault.tar"), Charsets.UTF8);
		try
		{
			assertEquals(10, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(UstarDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(4, d.getChildEntries().size());
			assertEquals(4943, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226162638"), d.getLastModificationTime());
			assertEquals(501, d.getOwnerGid());
			assertEquals(501, d.getOwnerUid());
			assertEquals("kalle", d.getOwnerGroupName());
			assertEquals("kalle", d.getOwnerUserName());
			assertEquals("", d.getUstarVersion());

			UstarDirectoryEntry d2 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(UstarDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(4985, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226162307"), d2.getLastModificationTime());
			assertEquals(501, d2.getOwnerGid());
			assertEquals(501, d2.getOwnerUid());
			assertEquals("kalle", d2.getOwnerGroupName());
			assertEquals("kalle", d2.getOwnerUserName());
			assertEquals("", d2.getUstarVersion());

			UstarDirectoryEntry d3 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(UstarDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(5183, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
			assertEquals(getUtcDate("20081226162307"), d3.getLastModificationTime());
			assertEquals(501, d3.getOwnerGid());
			assertEquals(501, d3.getOwnerUid());
			assertEquals("kalle", d3.getOwnerGroupName());
			assertEquals("kalle", d3.getOwnerUserName());
			assertEquals("", d3.getUstarVersion());

			UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(UstarFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(5335, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081226162315"), f1.getLastModificationTime());
			assertEquals(501, f1.getOwnerGid());
			assertEquals(501, f1.getOwnerUid());
			assertEquals("kalle", f1.getOwnerGroupName());
			assertEquals("kalle", f1.getOwnerUserName());
			assertEquals("", f1.getUstarVersion());
			assertEquals(7680, f1.getStartPosOfFileData());
			assertEquals(19, f1.getSize());
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			UstarFileEntry f2 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(UstarFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(5478, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081226162331"), f2.getLastModificationTime());
			assertEquals(501, f2.getOwnerGid());
			assertEquals(501, f2.getOwnerUid());
			assertEquals("kalle", f2.getOwnerGroupName());
			assertEquals("kalle", f2.getOwnerUserName());
			assertEquals("", f2.getUstarVersion());
			assertEquals(4608, f2.getStartPosOfFileData());
			assertEquals(19, f2.getSize());
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));

			// These are not the same å:s, ä:s and ö:s that Linux uses. Nice...
			UstarFileEntry fr = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/räksmörgås.txt"));
			assertSame(UstarFileEntry.class, fr.getClass());
			assertSame(fr, d.getChildEntries().get("räksmörgås.txt"));
			assertSame(d, fr.getParent());
			assertEquals(new AbsoluteLocation("/d/räksmörgås.txt"), fr.getLocation());
			assertEquals("räksmörgås.txt", fr.getName());
			assertEquals(7431, fr.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), fr.getEntityMode());
			assertEquals(getUtcDate("20081226162552"), fr.getLastModificationTime());
			assertEquals(501, fr.getOwnerGid());
			assertEquals(501, fr.getOwnerUid());
			assertEquals("kalle", fr.getOwnerGroupName());
			assertEquals("kalle", fr.getOwnerUserName());
			assertEquals("", fr.getUstarVersion());
			assertEquals(5632, fr.getStartPosOfFileData());
			assertEquals(26, fr.getSize());
			// Again another å, ä and ö...
			assertEquals("Contents of räksmörgås.txt", Files.readTextFile(fr, MAC_ROMAN));

			UstarSymbolicLinkEntry l1 = (UstarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/d/f1.txt"));
			assertSame(UstarSymbolicLinkEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt"), l1.getLocation());
			assertEquals("f1.txt", l1.getName());
			assertEquals(6165, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), l1.getEntityMode());
			assertEquals(getUtcDate("20081226162338"), l1.getLastModificationTime());
			assertEquals(501, l1.getOwnerGid());
			assertEquals(501, l1.getOwnerUid());
			assertEquals("kalle", l1.getOwnerGroupName());
			assertEquals("kalle", l1.getOwnerUserName());
			assertEquals("", l1.getUstarVersion());
			assertEquals(new RelativeLocation("../f1.txt"), l1.getLinkTarget());

			String name100 = getNumericalName(100);

			UstarFileEntry f10 = (UstarFileEntry) tf.get(new AbsoluteLocation("/" + name100));
			assertSame(UstarFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name100));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name100), f10.getLocation());
			assertEquals(name100, f10.getName());
			assertEquals(10042, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
			assertEquals(getUtcDate("20081226162634"), f10.getLastModificationTime());
			assertEquals(501, f10.getOwnerGid());
			assertEquals(501, f10.getOwnerUid());
			assertEquals("kalle", f10.getOwnerGroupName());
			assertEquals("kalle", f10.getOwnerUserName());
			assertEquals("", f10.getUstarVersion());
			assertEquals(512, f10.getStartPosOfFileData());
			assertEquals(22, f10.getSize());
			assertEquals("Contents of long file\n", Files.readTextFile(f10));

			UstarFileEntry f11 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d/" + name100));
			assertSame(UstarFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name100));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name100), f11.getLocation());
			assertEquals(name100, f11.getName());
			assertEquals(10080, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f11.getEntityMode());
			assertEquals(getUtcDate("20081226162638"), f11.getLastModificationTime());
			assertEquals(501, f11.getOwnerGid());
			assertEquals(501, f11.getOwnerUid());
			assertEquals("kalle", f11.getOwnerGroupName());
			assertEquals("kalle", f11.getOwnerUserName());
			assertEquals("", f11.getUstarVersion());
			assertEquals(3072, f11.getStartPosOfFileData());
			assertEquals(22, f11.getSize());
			assertEquals("Contents of long file\n", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testMacV7()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/macV7.tar"), Charsets.UTF8);
		try
		{
			assertEquals(10, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			TarDirectoryEntry d = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
			assertSame(TarDirectoryEntry.class, d.getClass());
			assertSame(d, root.getChildEntries().get("d"));
			assertSame(root, d.getParent());
			assertEquals(new AbsoluteLocation("/d"), d.getLocation());
			assertEquals("d", d.getName());
			assertEquals(4, d.getChildEntries().size());
			assertEquals(3279, d.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
			assertEquals(getUtcDate("20081226162742"), d.getLastModificationTime());
			assertEquals(501, d.getOwnerGid());
			assertEquals(501, d.getOwnerUid());

			TarDirectoryEntry d2 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d2"));
			assertSame(TarDirectoryEntry.class, d2.getClass());
			assertSame(d2, root.getChildEntries().get("d2"));
			assertSame(root, d2.getParent());
			assertEquals(new AbsoluteLocation("/d2"), d2.getLocation());
			assertEquals("d2", d2.getName());
			assertEquals(1, d2.getChildEntries().size());
			assertEquals(3320, d2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d2.getEntityMode());
			assertEquals(getUtcDate("20081226162307"), d2.getLastModificationTime());
			assertEquals(501, d2.getOwnerGid());
			assertEquals(501, d2.getOwnerUid());

			TarDirectoryEntry d3 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d2/d3"));
			assertSame(TarDirectoryEntry.class, d3.getClass());
			assertSame(d3, d2.getChildEntries().get("d3"));
			assertSame(d2, d3.getParent());
			assertEquals(new AbsoluteLocation("/d2/d3"), d3.getLocation());
			assertEquals("d3", d3.getName());
			assertEquals(0, d3.getChildEntries().size());
			assertEquals(3518, d3.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
			assertEquals(getUtcDate("20081226162307"), d3.getLastModificationTime());
			assertEquals(501, d3.getOwnerGid());
			assertEquals(501, d3.getOwnerUid());

			TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			assertSame(TarFileEntry.class, f1.getClass());
			assertSame(f1, root.getChildEntries().get("f1.txt"));
			assertSame(root, f1.getParent());
			assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
			assertEquals("f1.txt", f1.getName());
			assertEquals(3619, f1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
			assertEquals(getUtcDate("20081226165456"), f1.getLastModificationTime());
			assertEquals(501, f1.getOwnerGid());
			assertEquals(501, f1.getOwnerUid());
			assertEquals(6656, f1.getStartPosOfFileData());
			assertEquals(19, f1.getSize());
			assertEquals("Contents of f1.txt\n", Files.readTextFile(f1));

			TarFileEntry f2 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/f2.txt"));
			assertSame(TarFileEntry.class, f2.getClass());
			assertSame(f2, d.getChildEntries().get("f2.txt"));
			assertSame(d, f2.getParent());
			assertEquals(new AbsoluteLocation("/d/f2.txt"), f2.getLocation());
			assertEquals("f2.txt", f2.getName());
			assertEquals(3765, f2.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
			assertEquals(getUtcDate("20081226162331"), f2.getLastModificationTime());
			assertEquals(501, f2.getOwnerGid());
			assertEquals(501, f2.getOwnerUid());
			assertEquals(3584, f2.getStartPosOfFileData());
			assertEquals(19, f2.getSize());
			assertEquals("Contents of f2.txt\n", Files.readTextFile(f2));

			// These are not the same å:s, ä:s and ö:s that Linux uses. Nice...
			TarFileEntry fr = (TarFileEntry) tf.get(new AbsoluteLocation("/d/räksmörgås.txt"));
			assertSame(TarFileEntry.class, fr.getClass());
			assertSame(fr, d.getChildEntries().get("räksmörgås.txt"));
			assertSame(d, fr.getParent());
			assertEquals(new AbsoluteLocation("/d/räksmörgås.txt"), fr.getLocation());
			assertEquals("räksmörgås.txt", fr.getName());
			assertEquals(5718, fr.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), fr.getEntityMode());
			assertEquals(getUtcDate("20081226162552"), fr.getLastModificationTime());
			assertEquals(501, fr.getOwnerGid());
			assertEquals(501, fr.getOwnerUid());
			assertEquals(4608, fr.getStartPosOfFileData());
			assertEquals(26, fr.getSize());
			// Again another å, ä and ö...
			assertEquals("Contents of räksmörgås.txt", Files.readTextFile(fr, MAC_ROMAN));

			TarSymbolicLinkEntry l1 = (TarSymbolicLinkEntry) tf.get(new AbsoluteLocation("/d/f1.txt"));
			assertSame(TarSymbolicLinkEntry.class, l1.getClass());
			assertSame(l1, d.getChildEntries().get("f1.txt"));
			assertSame(d, l1.getParent());
			assertEquals(new AbsoluteLocation("/d/f1.txt"), l1.getLocation());
			assertEquals("f1.txt", l1.getName());
			assertEquals(4500, l1.getChecksum());
			assertEquals(UnixEntityMode.forCode(0755), l1.getEntityMode());
			assertEquals(getUtcDate("20081226162338"), l1.getLastModificationTime());
			assertEquals(501, l1.getOwnerGid());
			assertEquals(501, l1.getOwnerUid());
			assertEquals(new RelativeLocation("../f1.txt"), l1.getLinkTarget());

			String name100 = getNumericalName(100);

			TarFileEntry f10 = (TarFileEntry) tf.get(new AbsoluteLocation("/" + name100));
			assertSame(TarFileEntry.class, f10.getClass());
			assertSame(f10, root.getChildEntries().get(name100));
			assertSame(root, f10.getParent());
			assertEquals(new AbsoluteLocation("/" + name100), f10.getLocation());
			assertEquals(name100, f10.getName());
			assertEquals(8329, f10.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
			assertEquals(getUtcDate("20081226162634"), f10.getLastModificationTime());
			assertEquals(501, f10.getOwnerGid());
			assertEquals(501, f10.getOwnerUid());
			assertEquals(512, f10.getStartPosOfFileData());
			assertEquals(22, f10.getSize());
			assertEquals("Contents of long file\n", Files.readTextFile(f10));

			String name98 = getNumericalName(98);

			TarFileEntry f11 = (TarFileEntry) tf.get(new AbsoluteLocation("/d/" + name98));
			assertSame(TarFileEntry.class, f11.getClass());
			assertSame(f11, d.getChildEntries().get(name98));
			assertSame(d, f11.getParent());
			assertEquals(new AbsoluteLocation("/d/" + name98), f11.getLocation());
			assertEquals(name98, f11.getName());
			assertEquals(8367, f11.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f11.getEntityMode());
			assertEquals(getUtcDate("20081226162638"), f11.getLastModificationTime());
			assertEquals(501, f11.getOwnerGid());
			assertEquals(501, f11.getOwnerUid());
			assertEquals(2048, f11.getStartPosOfFileData());
			assertEquals(22, f11.getSize());
			assertEquals("Contents of long file\n", Files.readTextFile(f11));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testInterleavingReadsFromTwoDifferentFilesInArchive() throws IOException
	{
		TarFile tf = new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"));
		try
		{
			TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			TarFileEntry f2 = (TarFileEntry) tf.get(new AbsoluteLocation("/d1/d/f2.txt"));
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
			tf.close();
		}
	}

	@Test
	public void testInterleavingReadsFromSameFileInArchive() throws IOException
	{
		TarFile tf = new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"));
		try
		{

			TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
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
			tf.close();
		}
	}

	@Test
	public void testReadingFromClosedArchive() throws IOException
	{
		TarFile tf = new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"));
		try
		{

			TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
			Reader r1 = new InputStreamReader(f1.openForRead());
			try
			{
				tf.close();
				tf = null;

				char[] carr = new char[6];
				try
				{
					r1.read(carr);
					fail();
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
			if (tf != null)
			{
				tf.close();
			}
		}
	}

	@Test
	public void testEntryWithUnknownTypeFlag()
	{
		// This is singleFile.tar with the file's type flag modified using a
		// hex editor
		TarFile tf = new TarFile(getTestDataFile("tar/unknownTypeFlag.tar"));
		try
		{
			assertEquals(2, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/test.txt"));
			assertSame(f, root.getChildEntries().get("test.txt"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/test.txt"), f.getLocation());
			assertEquals("test.txt", f.getName());
			assertEquals(4958, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081222092338"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("", f.getUstarVersion());
			assertEquals(512, f.getStartPosOfFileData());
			assertEquals(28, f.getSize());
			assertEquals("Contents of test.txt åäö\n", Files.readTextFile(f, Charsets.UTF8));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testPaxNonAsciiCharactersInFileName()
	{
		// Codepage 1252 was NOT used for this archive. The Pax headers are
		// encoded in UTF-8, so this should work anyway.
		TarFile tf = new TarFile(getTestDataFile("tar/paxNonAsciiCharactersInFileName.tar"), CP1252);
		try
		{
			assertEquals(2, tf.size());

			TarDirectoryEntry root = tf.getRootEntry();
			assertHasStandardRootEntry(root);

			PaxFileEntry f = (PaxFileEntry) tf.get(new AbsoluteLocation("/räksmörgås.txt"));
			assertSame(f, root.getChildEntries().get("räksmörgås.txt"));
			assertSame(root, f.getParent());
			assertEquals(new AbsoluteLocation("/räksmörgås.txt"), f.getLocation());
			assertEquals("räksmörgås.txt", f.getName());
			assertEquals(7095, f.getChecksum());
			assertEquals(UnixEntityMode.forCode(0644), f.getEntityMode());
			assertEquals(getUtcDate("20081230174148"), f.getLastModificationTime());
			assertEquals(1000, f.getOwnerGid());
			assertEquals(1000, f.getOwnerUid());
			assertEquals("kalle", f.getOwnerGroupName());
			assertEquals("kalle", f.getOwnerUserName());
			assertEquals("00", f.getUstarVersion());
			assertEquals(1536, f.getStartPosOfFileData());
			assertEquals(30, f.getSize());
			assertEquals("Contents of räksmörgås.txt\n", Files.readTextFile(f, Charsets.UTF8));
		}
		finally
		{
			tf.close();
		}
	}

	@Test
	public void testRandomAccess()
	{
		TarFile tf = new TarFile(getTestDataFile("tar/someFilesAndDirectories.tar"));
		try
		{
			UstarFileEntry f = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
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
				assertEquals(19, ra.length());
				assertEquals('C', ra.read());
				byte[] barr = new byte[7];
				assertEquals(7, ra.read(barr));
				assertEquals("ontents", new String(barr));
				ra.seek(16);
				assertEquals(3, ra.read(barr));
				assertEquals("xt\nents", new String(barr));
				ra.seek(16);
				assertEquals(3, ra.read(barr, 2, 5));
				assertEquals("xtxt\nts", new String(barr));
				ra.seek(18);
				assertEquals('\n', ra.read());
				assertEquals(-1, ra.read());
				assertEquals(-1, ra.read(barr));
				assertEquals(-1, ra.read(barr, 2, 2));
				assertEquals(19, ra.getFilePointer());
			}
			finally
			{
				ra.close();
			}
		}
		finally
		{
			tf.close();
		}
	}
}
