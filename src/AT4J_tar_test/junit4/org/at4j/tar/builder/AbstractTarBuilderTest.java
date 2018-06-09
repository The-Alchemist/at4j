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
package org.at4j.tar.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.at4j.archive.builder.ArchiveBuilder;
import org.at4j.archive.builder.ArchiveEntryAddException;
import org.at4j.archive.builder.ArchiveEntrySettingsRule;
import org.at4j.archive.builder.NameGlobETAF;
import org.at4j.tar.AbstractTarFileTest;
import org.at4j.tar.PaxDirectoryEntry;
import org.at4j.tar.PaxFileEntry;
import org.at4j.tar.TarDirectoryEntry;
import org.at4j.tar.TarFile;
import org.at4j.tar.TarFileEntry;
import org.at4j.tar.UstarDirectoryEntry;
import org.at4j.tar.UstarFileEntry;
import org.at4j.test.support.TestFileSupport;
import org.entityfs.Directory;
import org.entityfs.ReadWritableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.ram.RamFileSystemBuilder;
import org.entityfs.support.nio.Charsets;
import org.entityfs.util.CharSequenceReadableFile;
import org.entityfs.util.Directories;
import org.entityfs.util.Files;
import org.entityfs.util.NamedReadableFileAdapter;
import org.entityfs.util.io.ReadWritableFileAdapter;
import org.junit.Test;

public abstract class AbstractTarBuilderTest<T extends ArchiveBuilder<T, TarEntrySettings>> extends AbstractTarFileTest
{
	protected ReadWritableFile createTargetFile()
	{
		return new ReadWritableFileAdapter(TestFileSupport.createTemporaryFile());
	}

	protected void assertHasStandardRootEntry(TarDirectoryEntry root)
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

	protected abstract T createTarBuilder(ReadWritableFile raf, TarBuilderSettings settings);

	/**
	 * Add all data from the supplied stream and then close it.
	 */
	protected abstract void addDataFromStream(T tb, InputStream is, AbsoluteLocation l, TarEntrySettings settings);

	@Test
	public void testAddEmptyFile()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new V7TarEntryStrategy()));
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile(""), "f1.txt"));
			tb.add(dir);

			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(3, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(TarFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals(512, f1.getStartPosOfFileData());
				assertEquals(0, f1.getSize());
				assertEquals("", Files.readTextFile(f1));

				TarDirectoryEntry d = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
				assertSame(TarDirectoryEntry.class, d.getClass());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(root, d.getParent());
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertEquals("d", d.getName());
				assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
				assertWithinLast30Seconds(d.getLastModificationTime());
				assertEquals(0, d.getOwnerGid());
				assertEquals(0, d.getOwnerUid());
				assertEquals(0, d.getChildEntries().size());
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testSimpleOldV7()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new V7TarEntryStrategy()));
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"));
			tb.add(dir);
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of file without parent dir"), "file_without_parent_dir.txt"), new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
			tb.add(dir, new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
			addDataFromStream(tb, new ByteArrayInputStream("Contents of f2.txt".getBytes()), new AbsoluteLocation("/f2.txt"), null);
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(8, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(TarFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals(512, f1.getStartPosOfFileData());
				assertEquals(18, f1.getSize());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));

				TarDirectoryEntry d = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
				assertSame(TarDirectoryEntry.class, d.getClass());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(root, d.getParent());
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertEquals("d", d.getName());
				assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
				assertWithinLast30Seconds(d.getLastModificationTime());
				assertEquals(0, d.getOwnerGid());
				assertEquals(0, d.getOwnerUid());
				assertEquals(0, d.getChildEntries().size());

				TarDirectoryEntry nff = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
				assertSame(TarDirectoryEntry.class, nff.getClass());
				assertSame(nff, root.getChildEntries().get("nonexisting_parent_dir_for_file"));
				assertSame(root, nff.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file"), nff.getLocation());
				assertEquals("nonexisting_parent_dir_for_file", nff.getName());
				assertEquals(UnixEntityMode.forCode(0755), nff.getEntityMode());
				assertWithinLast30Seconds(nff.getLastModificationTime());
				assertEquals(0, nff.getOwnerGid());
				assertEquals(0, nff.getOwnerUid());
				assertEquals(1, nff.getChildEntries().size());

				TarDirectoryEntry nfd = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
				assertSame(TarDirectoryEntry.class, nfd.getClass());
				assertSame(nfd, root.getChildEntries().get("nonexisting_parent_dir_for_directory"));
				assertSame(root, nfd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"), nfd.getLocation());
				assertEquals("nonexisting_parent_dir_for_directory", nfd.getName());
				assertEquals(UnixEntityMode.forCode(0755), nfd.getEntityMode());
				assertWithinLast30Seconds(nfd.getLastModificationTime());
				assertEquals(0, nfd.getOwnerGid());
				assertEquals(0, nfd.getOwnerUid());
				assertEquals(1, nfd.getChildEntries().size());

				TarFileEntry fwpd = (TarFileEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file/file_without_parent_dir.txt"));
				assertSame(TarFileEntry.class, fwpd.getClass());
				assertSame(fwpd, nff.getChildEntries().get("file_without_parent_dir.txt"));
				assertSame(nff, fwpd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file/file_without_parent_dir.txt"), fwpd.getLocation());
				assertEquals("file_without_parent_dir.txt", fwpd.getName());
				assertEquals(UnixEntityMode.forCode(0644), fwpd.getEntityMode());
				assertWithinLast30Seconds(fwpd.getLastModificationTime());
				assertEquals(0, fwpd.getOwnerGid());
				assertEquals(0, fwpd.getOwnerUid());
				assertEquals(2048, fwpd.getStartPosOfFileData());
				assertEquals(35, fwpd.getSize());
				assertEquals("Contents of file without parent dir", Files.readTextFile(fwpd));

				TarDirectoryEntry dwpd = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"));
				assertSame(TarDirectoryEntry.class, dwpd.getClass());
				assertSame(dwpd, nfd.getChildEntries().get("d"));
				assertSame(nfd, dwpd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"), dwpd.getLocation());
				assertEquals("d", dwpd.getName());
				assertEquals(UnixEntityMode.forCode(0755), dwpd.getEntityMode());
				assertWithinLast30Seconds(dwpd.getLastModificationTime());
				assertEquals(0, dwpd.getOwnerGid());
				assertEquals(0, dwpd.getOwnerUid());
				assertEquals(0, dwpd.getChildEntries().size());

				TarFileEntry f2 = (TarFileEntry) tf.get(new AbsoluteLocation("/f2.txt"));
				assertSame(TarFileEntry.class, f2.getClass());
				assertSame(f2, root.getChildEntries().get("f2.txt"));
				assertSame(root, f2.getParent());
				assertEquals(new AbsoluteLocation("/f2.txt"), f2.getLocation());
				assertEquals("f2.txt", f2.getName());
				assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
				assertWithinLast30Seconds(f2.getLastModificationTime());
				assertEquals(0, f2.getOwnerGid());
				assertEquals(0, f2.getOwnerUid());
				assertEquals(3584, f2.getStartPosOfFileData());
				assertEquals(18, f2.getSize());
				assertEquals("Contents of f2.txt", Files.readTextFile(f2));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testTooLongFileNameInOldV7()
	{
		ReadWritableFile tf = createTargetFile();
		try
		{
			T tb = createTarBuilder(tf, new TarBuilderSettings().setEntryStrategy(new V7TarEntryStrategy()));
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("foo"), getNumericalName(99)));
			try
			{
				tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("foo"), getNumericalName(100)));
				fail();
			}
			catch (ArchiveEntryAddException e)
			{
				assertTrue(e.getMessage().contains("name"));
			}
			tb.close();
		}
		finally
		{
			tf.delete();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddRecursivelyToOldV7() throws IOException
	{
		ReadWritableFile raf = createTargetFile();
		File dir1 = TestFileSupport.createTemporaryDir();
		try
		{
			File f1f = new File(dir1, "f1.txt");
			assertTrue(f1f.createNewFile());
			Files.writeText(new ReadWritableFileAdapter(f1f), "Contents of f1.txt");
			File d1f = new File(dir1, "d1");
			assertTrue(d1f.mkdir());
			assertTrue(new File(d1f, "d2").mkdir());

			Directory dir2 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");
			Files.writeText(Directories.newFile(dir2, "f10.txt"), "Contents of f10.txt");
			Directories.newDirectory(Directories.newDirectory(dir2, "d10"), "d20");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new V7TarEntryStrategy()));
			tb.addRecursively(dir1, AbsoluteLocation.ROOT_DIR);
			tb.addRecursively(dir2, AbsoluteLocation.ROOT_DIR);
			tb.addRecursively(dir2, new AbsoluteLocation("/d3"), null, new ArchiveEntrySettingsRule<TarEntrySettings>(new TarEntrySettings().setEntityMode(UnixEntityMode.forCode(0777)), new NameGlobETAF("d10")));
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(11, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(TarFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals(18, f1.getSize());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));

				TarDirectoryEntry d1 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d1"));
				assertSame(TarDirectoryEntry.class, d1.getClass());
				assertSame(d1, root.getChildEntries().get("d1"));
				assertSame(root, d1.getParent());
				assertEquals(new AbsoluteLocation("/d1"), d1.getLocation());
				assertEquals("d1", d1.getName());
				assertEquals(UnixEntityMode.forCode(0755), d1.getEntityMode());
				assertWithinLast30Seconds(d1.getLastModificationTime());
				assertEquals(0, d1.getOwnerGid());
				assertEquals(0, d1.getOwnerUid());
				assertEquals(1, d1.getChildEntries().size());

				TarDirectoryEntry d1d2 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d1/d2"));
				assertSame(TarDirectoryEntry.class, d1d2.getClass());
				assertSame(d1d2, d1.getChildEntries().get("d2"));
				assertSame(d1, d1d2.getParent());
				assertEquals(new AbsoluteLocation("/d1/d2"), d1d2.getLocation());
				assertEquals("d2", d1d2.getName());
				assertEquals(UnixEntityMode.forCode(0755), d1d2.getEntityMode());
				assertWithinLast30Seconds(d1d2.getLastModificationTime());
				assertEquals(0, d1d2.getOwnerGid());
				assertEquals(0, d1d2.getOwnerUid());
				assertEquals(0, d1d2.getChildEntries().size());

				TarFileEntry f10 = (TarFileEntry) tf.get(new AbsoluteLocation("/f10.txt"));
				assertSame(TarFileEntry.class, f10.getClass());
				assertSame(f10, root.getChildEntries().get("f10.txt"));
				assertSame(root, f10.getParent());
				assertEquals(new AbsoluteLocation("/f10.txt"), f10.getLocation());
				assertEquals("f10.txt", f10.getName());
				assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
				assertWithinLast30Seconds(f10.getLastModificationTime());
				assertEquals(0, f10.getOwnerGid());
				assertEquals(0, f10.getOwnerUid());
				assertEquals(19, f10.getSize());
				assertEquals("Contents of f10.txt", Files.readTextFile(f10));

				TarDirectoryEntry d10 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d10"));
				assertSame(TarDirectoryEntry.class, d10.getClass());
				assertSame(d10, root.getChildEntries().get("d10"));
				assertSame(root, d10.getParent());
				assertEquals(new AbsoluteLocation("/d10"), d10.getLocation());
				assertEquals("d10", d10.getName());
				assertEquals(UnixEntityMode.forCode(0755), d10.getEntityMode());
				assertWithinLast30Seconds(d10.getLastModificationTime());
				assertEquals(0, d10.getOwnerGid());
				assertEquals(0, d10.getOwnerUid());
				assertEquals(1, d10.getChildEntries().size());

				TarDirectoryEntry d10d20 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d10/d20"));
				assertSame(TarDirectoryEntry.class, d10d20.getClass());
				assertSame(d10d20, d10.getChildEntries().get("d20"));
				assertSame(d10, d10d20.getParent());
				assertEquals(new AbsoluteLocation("/d10/d20"), d10d20.getLocation());
				assertEquals("d20", d10d20.getName());
				assertEquals(UnixEntityMode.forCode(0755), d10d20.getEntityMode());
				assertWithinLast30Seconds(d10d20.getLastModificationTime());
				assertEquals(0, d10d20.getOwnerGid());
				assertEquals(0, d10d20.getOwnerUid());
				assertEquals(0, d10d20.getChildEntries().size());

				TarDirectoryEntry d3 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d3"));
				assertSame(TarDirectoryEntry.class, d3.getClass());
				assertSame(d3, root.getChildEntries().get("d3"));
				assertSame(root, d3.getParent());
				assertEquals(new AbsoluteLocation("/d3"), d3.getLocation());
				assertEquals("d3", d3.getName());
				assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
				assertWithinLast30Seconds(d3.getLastModificationTime());
				assertEquals(0, d3.getOwnerGid());
				assertEquals(0, d3.getOwnerUid());
				assertEquals(2, d3.getChildEntries().size());

				TarFileEntry d3f10 = (TarFileEntry) tf.get(new AbsoluteLocation("/d3/f10.txt"));
				assertSame(TarFileEntry.class, d3f10.getClass());
				assertSame(d3f10, d3.getChildEntries().get("f10.txt"));
				assertSame(d3, d3f10.getParent());
				assertEquals(new AbsoluteLocation("/d3/f10.txt"), d3f10.getLocation());
				assertEquals("f10.txt", d3f10.getName());
				assertEquals(UnixEntityMode.forCode(0644), d3f10.getEntityMode());
				assertWithinLast30Seconds(d3f10.getLastModificationTime());
				assertEquals(0, d3f10.getOwnerGid());
				assertEquals(0, d3f10.getOwnerUid());
				assertEquals(19, d3f10.getSize());
				assertEquals("Contents of f10.txt", Files.readTextFile(d3f10));

				TarDirectoryEntry d3d10 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d3/d10"));
				assertSame(TarDirectoryEntry.class, d3d10.getClass());
				assertSame(d3d10, d3.getChildEntries().get("d10"));
				assertSame(d3, d3d10.getParent());
				assertEquals(new AbsoluteLocation("/d3/d10"), d3d10.getLocation());
				assertEquals("d10", d3d10.getName());
				assertEquals(UnixEntityMode.forCode(0777), d3d10.getEntityMode());
				assertWithinLast30Seconds(d3d10.getLastModificationTime());
				assertEquals(0, d3d10.getOwnerGid());
				assertEquals(0, d3d10.getOwnerUid());
				assertEquals(1, d3d10.getChildEntries().size());

				TarDirectoryEntry d3d10d20 = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/d3/d10/d20"));
				assertSame(TarDirectoryEntry.class, d3d10d20.getClass());
				assertSame(d3d10d20, d3d10.getChildEntries().get("d20"));
				assertSame(d3d10, d3d10d20.getParent());
				assertEquals(new AbsoluteLocation("/d3/d10/d20"), d3d10d20.getLocation());
				assertEquals("d20", d3d10d20.getName());
				assertEquals(UnixEntityMode.forCode(0755), d3d10d20.getEntityMode());
				assertWithinLast30Seconds(d3d10d20.getLastModificationTime());
				assertEquals(0, d3d10d20.getOwnerGid());
				assertEquals(0, d3d10d20.getOwnerUid());
				assertEquals(0, d3d10d20.getChildEntries().size());
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir1);
		}
	}

	private void testSimpleUstarCompatible(TarEntryStrategy strat)
	{
		String user = System.getProperty("user.name");
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(strat));
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"));
			tb.add(dir);
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of file without parent dir"), "file_without_parent_dir.txt"), new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
			tb.add(dir, new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
			addDataFromStream(tb, new ByteArrayInputStream("Contents of f2.txt".getBytes()), new AbsoluteLocation("/f2.txt"), new TarEntrySettings().setOwnerUserName("root").setOwnerGroupName("wheel"));
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(8, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(UstarFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals("00", f1.getUstarVersion());
				assertEquals(user, f1.getOwnerUserName());
				assertEquals("users", f1.getOwnerGroupName());
				assertEquals(512, f1.getStartPosOfFileData());
				assertEquals(18, f1.getSize());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));

				UstarDirectoryEntry d = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
				assertSame(UstarDirectoryEntry.class, d.getClass());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(root, d.getParent());
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertEquals("d", d.getName());
				assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
				assertWithinLast30Seconds(d.getLastModificationTime());
				assertEquals(0, d.getOwnerGid());
				assertEquals(0, d.getOwnerUid());
				assertEquals("00", d.getUstarVersion());
				assertEquals(user, d.getOwnerUserName());
				assertEquals("users", d.getOwnerGroupName());
				assertEquals(0, d.getChildEntries().size());

				TarDirectoryEntry nff = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
				assertSame(TarDirectoryEntry.class, nff.getClass());
				assertSame(nff, root.getChildEntries().get("nonexisting_parent_dir_for_file"));
				assertSame(root, nff.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file"), nff.getLocation());
				assertEquals("nonexisting_parent_dir_for_file", nff.getName());
				assertEquals(UnixEntityMode.forCode(0755), nff.getEntityMode());
				assertWithinLast30Seconds(nff.getLastModificationTime());
				assertEquals(0, nff.getOwnerGid());
				assertEquals(0, nff.getOwnerUid());
				assertEquals(1, nff.getChildEntries().size());

				TarDirectoryEntry nfd = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
				assertSame(TarDirectoryEntry.class, nfd.getClass());
				assertSame(nfd, root.getChildEntries().get("nonexisting_parent_dir_for_directory"));
				assertSame(root, nfd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"), nfd.getLocation());
				assertEquals("nonexisting_parent_dir_for_directory", nfd.getName());
				assertEquals(UnixEntityMode.forCode(0755), nfd.getEntityMode());
				assertWithinLast30Seconds(nfd.getLastModificationTime());
				assertEquals(0, nfd.getOwnerGid());
				assertEquals(0, nfd.getOwnerUid());
				assertEquals(1, nfd.getChildEntries().size());

				UstarFileEntry fwpd = (UstarFileEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file/file_without_parent_dir.txt"));
				assertSame(UstarFileEntry.class, fwpd.getClass());
				assertSame(fwpd, nff.getChildEntries().get("file_without_parent_dir.txt"));
				assertSame(nff, fwpd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file/file_without_parent_dir.txt"), fwpd.getLocation());
				assertEquals("file_without_parent_dir.txt", fwpd.getName());
				assertEquals(UnixEntityMode.forCode(0644), fwpd.getEntityMode());
				assertWithinLast30Seconds(fwpd.getLastModificationTime());
				assertEquals(0, fwpd.getOwnerGid());
				assertEquals(0, fwpd.getOwnerUid());
				assertEquals("00", fwpd.getUstarVersion());
				assertEquals(user, fwpd.getOwnerUserName());
				assertEquals("users", fwpd.getOwnerGroupName());
				assertEquals(2048, fwpd.getStartPosOfFileData());
				assertEquals(35, fwpd.getSize());
				assertEquals("Contents of file without parent dir", Files.readTextFile(fwpd));

				UstarDirectoryEntry dwpd = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"));
				assertSame(UstarDirectoryEntry.class, dwpd.getClass());
				assertSame(dwpd, nfd.getChildEntries().get("d"));
				assertSame(nfd, dwpd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"), dwpd.getLocation());
				assertEquals("d", dwpd.getName());
				assertEquals(UnixEntityMode.forCode(0755), dwpd.getEntityMode());
				assertWithinLast30Seconds(dwpd.getLastModificationTime());
				assertEquals(0, dwpd.getOwnerGid());
				assertEquals(0, dwpd.getOwnerUid());
				assertEquals("00", dwpd.getUstarVersion());
				assertEquals(user, dwpd.getOwnerUserName());
				assertEquals("users", dwpd.getOwnerGroupName());
				assertEquals(0, dwpd.getChildEntries().size());

				UstarFileEntry f2 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f2.txt"));
				assertSame(UstarFileEntry.class, f2.getClass());
				assertSame(f2, root.getChildEntries().get("f2.txt"));
				assertSame(root, f2.getParent());
				assertEquals(new AbsoluteLocation("/f2.txt"), f2.getLocation());
				assertEquals("f2.txt", f2.getName());
				assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
				assertWithinLast30Seconds(f2.getLastModificationTime());
				assertEquals(0, f2.getOwnerGid());
				assertEquals(0, f2.getOwnerUid());
				assertEquals("00", f2.getUstarVersion());
				assertEquals("root", f2.getOwnerUserName());
				assertEquals("wheel", f2.getOwnerGroupName());
				assertEquals(3584, f2.getStartPosOfFileData());
				assertEquals(18, f2.getSize());
				assertEquals("Contents of f2.txt", Files.readTextFile(f2));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testSimpleUstar()
	{
		testSimpleUstarCompatible(new UstarEntryStrategy());
	}

	@Test
	public void testLongFileNamesInUstar()
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new UstarEntryStrategy()));
			try
			{
				tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), getNumericalName(100)));
				fail();
			}
			catch (ArchiveEntryAddException e)
			{
				// ok
			}

			String s155 = getNumericalName(155);
			String s99 = getNumericalName(99);
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of s99"), s99), new AbsoluteLocation("/" + s155));

			try
			{
				tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"), new AbsoluteLocation("/" + getNumericalName(100) + "/" + getNumericalName(55)));
				fail();
			}
			catch (ArchiveEntryAddException e)
			{
				// ok
			}
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(3, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/" + s155 + "/" + s99));
				assertSame(UstarFileEntry.class, f1.getClass());
				assertEquals(new AbsoluteLocation("/" + s155 + "/" + s99), f1.getLocation());
				assertEquals(s99, f1.getName());
				assertEquals("Contents of s99", Files.readTextFile(f1));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@SuppressWarnings("unchecked")
	private void testAddRecursivelyToUstarCompatible(TarEntryStrategy strat) throws IOException
	{
		String user = System.getProperty("user.name");
		ReadWritableFile raf = createTargetFile();
		File dir1 = TestFileSupport.createTemporaryDir();
		try
		{
			File f1f = new File(dir1, "f1.txt");
			assertTrue(f1f.createNewFile());
			Files.writeText(new ReadWritableFileAdapter(f1f), "Contents of f1.txt");
			File d1f = new File(dir1, "d1");
			assertTrue(d1f.mkdir());
			assertTrue(new File(d1f, "d2").mkdir());

			Directory dir2 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");
			Files.writeText(Directories.newFile(dir2, "f10.txt"), "Contents of f10.txt");
			Directories.newDirectory(Directories.newDirectory(dir2, "d10"), "d20");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(strat));
			tb.addRecursively(dir1, AbsoluteLocation.ROOT_DIR);
			tb.addRecursively(dir2, AbsoluteLocation.ROOT_DIR);
			tb.addRecursively(dir2, new AbsoluteLocation("/d3"), null, new ArchiveEntrySettingsRule<TarEntrySettings>(new TarEntrySettings().setEntityMode(UnixEntityMode.forCode(0777)), new NameGlobETAF("d10")));
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(11, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				UstarFileEntry f1 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(UstarFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals(user, f1.getOwnerUserName());
				assertEquals("users", f1.getOwnerGroupName());
				assertEquals("00", f1.getUstarVersion());
				assertEquals(18, f1.getSize());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));

				UstarDirectoryEntry d1 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d1"));
				assertSame(UstarDirectoryEntry.class, d1.getClass());
				assertSame(d1, root.getChildEntries().get("d1"));
				assertSame(root, d1.getParent());
				assertEquals(new AbsoluteLocation("/d1"), d1.getLocation());
				assertEquals("d1", d1.getName());
				assertEquals(UnixEntityMode.forCode(0755), d1.getEntityMode());
				assertWithinLast30Seconds(d1.getLastModificationTime());
				assertEquals(0, d1.getOwnerGid());
				assertEquals(0, d1.getOwnerUid());
				assertEquals(user, d1.getOwnerUserName());
				assertEquals("users", d1.getOwnerGroupName());
				assertEquals("00", d1.getUstarVersion());
				assertEquals(1, d1.getChildEntries().size());

				UstarDirectoryEntry d1d2 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d1/d2"));
				assertSame(UstarDirectoryEntry.class, d1d2.getClass());
				assertSame(d1d2, d1.getChildEntries().get("d2"));
				assertSame(d1, d1d2.getParent());
				assertEquals(new AbsoluteLocation("/d1/d2"), d1d2.getLocation());
				assertEquals("d2", d1d2.getName());
				assertEquals(UnixEntityMode.forCode(0755), d1d2.getEntityMode());
				assertWithinLast30Seconds(d1d2.getLastModificationTime());
				assertEquals(0, d1d2.getOwnerGid());
				assertEquals(0, d1d2.getOwnerUid());
				assertEquals(user, d1d2.getOwnerUserName());
				assertEquals("users", d1d2.getOwnerGroupName());
				assertEquals("00", d1d2.getUstarVersion());
				assertEquals(0, d1d2.getChildEntries().size());

				UstarFileEntry f10 = (UstarFileEntry) tf.get(new AbsoluteLocation("/f10.txt"));
				assertSame(UstarFileEntry.class, f10.getClass());
				assertSame(f10, root.getChildEntries().get("f10.txt"));
				assertSame(root, f10.getParent());
				assertEquals(new AbsoluteLocation("/f10.txt"), f10.getLocation());
				assertEquals("f10.txt", f10.getName());
				assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
				assertWithinLast30Seconds(f10.getLastModificationTime());
				assertEquals(0, f10.getOwnerGid());
				assertEquals(0, f10.getOwnerUid());
				assertEquals(user, f10.getOwnerUserName());
				assertEquals("users", f10.getOwnerGroupName());
				assertEquals("00", f10.getUstarVersion());
				assertEquals(19, f10.getSize());
				assertEquals("Contents of f10.txt", Files.readTextFile(f10));

				UstarDirectoryEntry d10 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d10"));
				assertSame(UstarDirectoryEntry.class, d10.getClass());
				assertSame(d10, root.getChildEntries().get("d10"));
				assertSame(root, d10.getParent());
				assertEquals(new AbsoluteLocation("/d10"), d10.getLocation());
				assertEquals("d10", d10.getName());
				assertEquals(UnixEntityMode.forCode(0755), d10.getEntityMode());
				assertWithinLast30Seconds(d10.getLastModificationTime());
				assertEquals(0, d10.getOwnerGid());
				assertEquals(0, d10.getOwnerUid());
				assertEquals(user, d10.getOwnerUserName());
				assertEquals("users", d10.getOwnerGroupName());
				assertEquals("00", d10.getUstarVersion());
				assertEquals(1, d10.getChildEntries().size());

				UstarDirectoryEntry d10d20 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d10/d20"));
				assertSame(UstarDirectoryEntry.class, d10d20.getClass());
				assertSame(d10d20, d10.getChildEntries().get("d20"));
				assertSame(d10, d10d20.getParent());
				assertEquals(new AbsoluteLocation("/d10/d20"), d10d20.getLocation());
				assertEquals("d20", d10d20.getName());
				assertEquals(UnixEntityMode.forCode(0755), d10d20.getEntityMode());
				assertWithinLast30Seconds(d10d20.getLastModificationTime());
				assertEquals(0, d10d20.getOwnerGid());
				assertEquals(0, d10d20.getOwnerUid());
				assertEquals(user, d10d20.getOwnerUserName());
				assertEquals("users", d10d20.getOwnerGroupName());
				assertEquals("00", d10d20.getUstarVersion());
				assertEquals(0, d10d20.getChildEntries().size());

				UstarDirectoryEntry d3 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d3"));
				assertSame(UstarDirectoryEntry.class, d3.getClass());
				assertSame(d3, root.getChildEntries().get("d3"));
				assertSame(root, d3.getParent());
				assertEquals(new AbsoluteLocation("/d3"), d3.getLocation());
				assertEquals("d3", d3.getName());
				assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
				assertWithinLast30Seconds(d3.getLastModificationTime());
				assertEquals(0, d3.getOwnerGid());
				assertEquals(0, d3.getOwnerUid());
				assertEquals(user, d3.getOwnerUserName());
				assertEquals("users", d3.getOwnerGroupName());
				assertEquals("00", d3.getUstarVersion());
				assertEquals(2, d3.getChildEntries().size());

				UstarFileEntry d3f10 = (UstarFileEntry) tf.get(new AbsoluteLocation("/d3/f10.txt"));
				assertSame(UstarFileEntry.class, d3f10.getClass());
				assertSame(d3f10, d3.getChildEntries().get("f10.txt"));
				assertSame(d3, d3f10.getParent());
				assertEquals(new AbsoluteLocation("/d3/f10.txt"), d3f10.getLocation());
				assertEquals("f10.txt", d3f10.getName());
				assertEquals(UnixEntityMode.forCode(0644), d3f10.getEntityMode());
				assertWithinLast30Seconds(d3f10.getLastModificationTime());
				assertEquals(0, d3f10.getOwnerGid());
				assertEquals(0, d3f10.getOwnerUid());
				assertEquals(user, d3f10.getOwnerUserName());
				assertEquals("users", d3f10.getOwnerGroupName());
				assertEquals("00", d3f10.getUstarVersion());
				assertEquals(19, d3f10.getSize());
				assertEquals("Contents of f10.txt", Files.readTextFile(d3f10));

				UstarDirectoryEntry d3d10 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d3/d10"));
				assertSame(UstarDirectoryEntry.class, d3d10.getClass());
				assertSame(d3d10, d3.getChildEntries().get("d10"));
				assertSame(d3, d3d10.getParent());
				assertEquals(new AbsoluteLocation("/d3/d10"), d3d10.getLocation());
				assertEquals("d10", d3d10.getName());
				assertEquals(UnixEntityMode.forCode(0777), d3d10.getEntityMode());
				assertWithinLast30Seconds(d3d10.getLastModificationTime());
				assertEquals(0, d3d10.getOwnerGid());
				assertEquals(0, d3d10.getOwnerUid());
				assertEquals(user, d3d10.getOwnerUserName());
				assertEquals("users", d3d10.getOwnerGroupName());
				assertEquals("00", d3d10.getUstarVersion());
				assertEquals(1, d3d10.getChildEntries().size());

				UstarDirectoryEntry d3d10d20 = (UstarDirectoryEntry) tf.get(new AbsoluteLocation("/d3/d10/d20"));
				assertSame(UstarDirectoryEntry.class, d3d10d20.getClass());
				assertSame(d3d10d20, d3d10.getChildEntries().get("d20"));
				assertSame(d3d10, d3d10d20.getParent());
				assertEquals(new AbsoluteLocation("/d3/d10/d20"), d3d10d20.getLocation());
				assertEquals("d20", d3d10d20.getName());
				assertEquals(UnixEntityMode.forCode(0755), d3d10d20.getEntityMode());
				assertWithinLast30Seconds(d3d10d20.getLastModificationTime());
				assertEquals(0, d3d10d20.getOwnerGid());
				assertEquals(0, d3d10d20.getOwnerUid());
				assertEquals(user, d3d10d20.getOwnerUserName());
				assertEquals("users", d3d10d20.getOwnerGroupName());
				assertEquals("00", d3d10d20.getUstarVersion());
				assertEquals(0, d3d10d20.getChildEntries().size());
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir1);
		}
	}

	@Test
	public void testAddRecursivelyToUstar() throws IOException
	{
		testAddRecursivelyToUstarCompatible(new UstarEntryStrategy());
	}

	@Test
	public void testSimpleGnuTar()
	{
		testSimpleUstarCompatible(new GnuTarEntryStrategy());
	}

	private void testTarLongFileNameForEntriesThatSupportIt(TarEntryStrategy strat)
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(strat));

			String s240 = getNumericalName(240);
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of s240"), s240), new AbsoluteLocation("/" + s240 + "/" + s240));
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(4, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				TarFileEntry f1 = (TarFileEntry) tf.get(new AbsoluteLocation("/" + s240 + "/" + s240 + "/" + s240));
				assertEquals(new AbsoluteLocation("/" + s240 + "/" + s240 + "/" + s240), f1.getLocation());
				assertEquals(s240, f1.getName());
				assertEquals("Contents of s240", Files.readTextFile(f1));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testGnuTarLongFileName()
	{
		testTarLongFileNameForEntriesThatSupportIt(new GnuTarEntryStrategy());
	}

	@Test
	public void testAddRecursivelyToGnuTar() throws IOException
	{
		testAddRecursivelyToUstarCompatible(new GnuTarEntryStrategy());
	}

	private Date parsePaxDate(String s)
	{
		String[] sarr = s.split("\\.");
		long time = Long.parseLong(sarr[0]) * 1000;
		time += Long.parseLong(sarr[1]) / 1000000;
		return new Date(time);
	}

	@Test
	public void testSimplePaxTar()
	{
		String user = System.getProperty("user.name");
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new PaxTarEntryStrategy().addPaxVariableProvider(new PaxVariableForTestProvider())));
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"));
			tb.add(dir);
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of file without parent dir"), "file_without_parent_dir.txt"), new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
			tb.add(dir, new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
			addDataFromStream(tb, new ByteArrayInputStream("Contents of f2.txt".getBytes()), new AbsoluteLocation("/f2.txt"), new TarEntrySettings().setOwnerUserName("root").setOwnerGroupName("wheel"));
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(8, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				PaxFileEntry f1 = (PaxFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(PaxFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals("00", f1.getUstarVersion());
				assertEquals(user, f1.getOwnerUserName());
				assertEquals("users", f1.getOwnerGroupName());
				assertEquals(2, f1.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(f1.getPaxVariables().get("mtime")));
				assertEquals("trueåäö", f1.getPaxVariables().get("is_f1.txt"));
				assertEquals(1536, f1.getStartPosOfFileData());
				assertEquals(18, f1.getSize());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));

				PaxDirectoryEntry d = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d"));
				assertSame(PaxDirectoryEntry.class, d.getClass());
				assertSame(d, root.getChildEntries().get("d"));
				assertSame(root, d.getParent());
				assertEquals(new AbsoluteLocation("/d"), d.getLocation());
				assertEquals("d", d.getName());
				assertEquals(UnixEntityMode.forCode(0755), d.getEntityMode());
				assertWithinLast30Seconds(d.getLastModificationTime());
				assertEquals(0, d.getOwnerGid());
				assertEquals(0, d.getOwnerUid());
				assertEquals("00", d.getUstarVersion());
				assertEquals(user, d.getOwnerUserName());
				assertEquals("users", d.getOwnerGroupName());
				assertEquals(1, d.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d.getPaxVariables().get("mtime")));
				assertEquals(0, d.getChildEntries().size());

				TarDirectoryEntry nff = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
				assertSame(TarDirectoryEntry.class, nff.getClass());
				assertSame(nff, root.getChildEntries().get("nonexisting_parent_dir_for_file"));
				assertSame(root, nff.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file"), nff.getLocation());
				assertEquals("nonexisting_parent_dir_for_file", nff.getName());
				assertEquals(UnixEntityMode.forCode(0755), nff.getEntityMode());
				assertWithinLast30Seconds(nff.getLastModificationTime());
				assertEquals(0, nff.getOwnerGid());
				assertEquals(0, nff.getOwnerUid());
				assertEquals(1, nff.getChildEntries().size());

				TarDirectoryEntry nfd = (TarDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
				assertSame(TarDirectoryEntry.class, nfd.getClass());
				assertSame(nfd, root.getChildEntries().get("nonexisting_parent_dir_for_directory"));
				assertSame(root, nfd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory"), nfd.getLocation());
				assertEquals("nonexisting_parent_dir_for_directory", nfd.getName());
				assertEquals(UnixEntityMode.forCode(0755), nfd.getEntityMode());
				assertWithinLast30Seconds(nfd.getLastModificationTime());
				assertEquals(0, nfd.getOwnerGid());
				assertEquals(0, nfd.getOwnerUid());
				assertEquals(1, nfd.getChildEntries().size());

				PaxFileEntry fwpd = (PaxFileEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_file/file_without_parent_dir.txt"));
				assertSame(PaxFileEntry.class, fwpd.getClass());
				assertSame(fwpd, nff.getChildEntries().get("file_without_parent_dir.txt"));
				assertSame(nff, fwpd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_file/file_without_parent_dir.txt"), fwpd.getLocation());
				assertEquals("file_without_parent_dir.txt", fwpd.getName());
				assertEquals(UnixEntityMode.forCode(0644), fwpd.getEntityMode());
				assertWithinLast30Seconds(fwpd.getLastModificationTime());
				assertEquals(0, fwpd.getOwnerGid());
				assertEquals(0, fwpd.getOwnerUid());
				assertEquals("00", fwpd.getUstarVersion());
				assertEquals(user, fwpd.getOwnerUserName());
				assertEquals("users", fwpd.getOwnerGroupName());
				assertEquals(1, fwpd.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(fwpd.getPaxVariables().get("mtime")));
				assertEquals(5120, fwpd.getStartPosOfFileData());
				assertEquals(35, fwpd.getSize());
				assertEquals("Contents of file without parent dir", Files.readTextFile(fwpd));

				PaxDirectoryEntry dwpd = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"));
				assertSame(PaxDirectoryEntry.class, dwpd.getClass());
				assertSame(dwpd, nfd.getChildEntries().get("d"));
				assertSame(nfd, dwpd.getParent());
				assertEquals(new AbsoluteLocation("/nonexisting_parent_dir_for_directory/d"), dwpd.getLocation());
				assertEquals("d", dwpd.getName());
				assertEquals(UnixEntityMode.forCode(0755), dwpd.getEntityMode());
				assertWithinLast30Seconds(dwpd.getLastModificationTime());
				assertEquals(0, dwpd.getOwnerGid());
				assertEquals(0, dwpd.getOwnerUid());
				assertEquals("00", dwpd.getUstarVersion());
				assertEquals(user, dwpd.getOwnerUserName());
				assertEquals("users", dwpd.getOwnerGroupName());
				assertEquals(1, dwpd.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(dwpd.getPaxVariables().get("mtime")));
				assertEquals(0, dwpd.getChildEntries().size());

				PaxFileEntry f2 = (PaxFileEntry) tf.get(new AbsoluteLocation("/f2.txt"));
				assertSame(PaxFileEntry.class, f2.getClass());
				assertSame(f2, root.getChildEntries().get("f2.txt"));
				assertSame(root, f2.getParent());
				assertEquals(new AbsoluteLocation("/f2.txt"), f2.getLocation());
				assertEquals("f2.txt", f2.getName());
				assertEquals(UnixEntityMode.forCode(0644), f2.getEntityMode());
				assertWithinLast30Seconds(f2.getLastModificationTime());
				assertEquals(0, f2.getOwnerGid());
				assertEquals(0, f2.getOwnerUid());
				assertEquals("00", f2.getUstarVersion());
				assertEquals("root", f2.getOwnerUserName());
				assertEquals("wheel", f2.getOwnerGroupName());
				assertEquals(1, f2.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(f2.getPaxVariables().get("mtime")));
				assertEquals(8704, f2.getStartPosOfFileData());
				assertEquals(18, f2.getSize());
				assertEquals("Contents of f2.txt", Files.readTextFile(f2));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}

	@Test
	public void testPaxTarLongFileName()
	{
		testTarLongFileNameForEntriesThatSupportIt(new PaxTarEntryStrategy());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddRecursivelyToPaxTar() throws IOException
	{
		String user = System.getProperty("user.name");
		ReadWritableFile raf = createTargetFile();
		File dir1 = TestFileSupport.createTemporaryDir();
		try
		{
			File f1f = new File(dir1, "f1.txt");
			assertTrue(f1f.createNewFile());
			Files.writeText(new ReadWritableFileAdapter(f1f), "Contents of f1.txt");
			File d1f = new File(dir1, "d1");
			assertTrue(d1f.mkdir());
			assertTrue(new File(d1f, "d2").mkdir());

			Directory dir2 = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");
			Files.writeText(Directories.newFile(dir2, "f10.txt"), "Contents of f10.txt");
			Directories.newDirectory(Directories.newDirectory(dir2, "d10"), "d20");

			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new PaxTarEntryStrategy().addPaxVariableProvider(new PaxVariableForTestProvider())));
			tb.addRecursively(dir1, AbsoluteLocation.ROOT_DIR);
			tb.addRecursively(dir2, AbsoluteLocation.ROOT_DIR);
			tb.addRecursively(dir2, new AbsoluteLocation("/d3"), null, new ArchiveEntrySettingsRule<TarEntrySettings>(new TarEntrySettings().setEntityMode(UnixEntityMode.forCode(0777)), new NameGlobETAF("d10")));
			tb.close();

			TarFile tf = new TarFile(raf);
			try
			{
				assertEquals(11, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				PaxFileEntry f1 = (PaxFileEntry) tf.get(new AbsoluteLocation("/f1.txt"));
				assertSame(PaxFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("f1.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/f1.txt"), f1.getLocation());
				assertEquals("f1.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals(user, f1.getOwnerUserName());
				assertEquals("users", f1.getOwnerGroupName());
				assertEquals("00", f1.getUstarVersion());
				assertEquals(2, f1.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(f1.getPaxVariables().get("mtime")));
				assertEquals("trueåäö", f1.getPaxVariables().get("is_f1.txt"));
				assertEquals(18, f1.getSize());
				assertEquals("Contents of f1.txt", Files.readTextFile(f1));

				PaxDirectoryEntry d1 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d1"));
				assertSame(PaxDirectoryEntry.class, d1.getClass());
				assertSame(d1, root.getChildEntries().get("d1"));
				assertSame(root, d1.getParent());
				assertEquals(new AbsoluteLocation("/d1"), d1.getLocation());
				assertEquals("d1", d1.getName());
				assertEquals(UnixEntityMode.forCode(0755), d1.getEntityMode());
				assertWithinLast30Seconds(d1.getLastModificationTime());
				assertEquals(0, d1.getOwnerGid());
				assertEquals(0, d1.getOwnerUid());
				assertEquals(user, d1.getOwnerUserName());
				assertEquals("users", d1.getOwnerGroupName());
				assertEquals("00", d1.getUstarVersion());
				assertEquals(1, d1.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d1.getPaxVariables().get("mtime")));
				assertEquals(1, d1.getChildEntries().size());

				PaxDirectoryEntry d1d2 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d1/d2"));
				assertSame(PaxDirectoryEntry.class, d1d2.getClass());
				assertSame(d1d2, d1.getChildEntries().get("d2"));
				assertSame(d1, d1d2.getParent());
				assertEquals(new AbsoluteLocation("/d1/d2"), d1d2.getLocation());
				assertEquals("d2", d1d2.getName());
				assertEquals(UnixEntityMode.forCode(0755), d1d2.getEntityMode());
				assertWithinLast30Seconds(d1d2.getLastModificationTime());
				assertEquals(0, d1d2.getOwnerGid());
				assertEquals(0, d1d2.getOwnerUid());
				assertEquals(user, d1d2.getOwnerUserName());
				assertEquals("users", d1d2.getOwnerGroupName());
				assertEquals("00", d1d2.getUstarVersion());
				assertEquals(1, d1d2.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d1d2.getPaxVariables().get("mtime")));
				assertEquals(0, d1d2.getChildEntries().size());

				PaxFileEntry f10 = (PaxFileEntry) tf.get(new AbsoluteLocation("/f10.txt"));
				assertSame(PaxFileEntry.class, f10.getClass());
				assertSame(f10, root.getChildEntries().get("f10.txt"));
				assertSame(root, f10.getParent());
				assertEquals(new AbsoluteLocation("/f10.txt"), f10.getLocation());
				assertEquals("f10.txt", f10.getName());
				assertEquals(UnixEntityMode.forCode(0644), f10.getEntityMode());
				assertWithinLast30Seconds(f10.getLastModificationTime());
				assertEquals(0, f10.getOwnerGid());
				assertEquals(0, f10.getOwnerUid());
				assertEquals(user, f10.getOwnerUserName());
				assertEquals("users", f10.getOwnerGroupName());
				assertEquals("00", f10.getUstarVersion());
				assertEquals(1, f10.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(f10.getPaxVariables().get("mtime")));
				assertEquals(19, f10.getSize());
				assertEquals("Contents of f10.txt", Files.readTextFile(f10));

				PaxDirectoryEntry d10 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d10"));
				assertSame(PaxDirectoryEntry.class, d10.getClass());
				assertSame(d10, root.getChildEntries().get("d10"));
				assertSame(root, d10.getParent());
				assertEquals(new AbsoluteLocation("/d10"), d10.getLocation());
				assertEquals("d10", d10.getName());
				assertEquals(UnixEntityMode.forCode(0755), d10.getEntityMode());
				assertWithinLast30Seconds(d10.getLastModificationTime());
				assertEquals(0, d10.getOwnerGid());
				assertEquals(0, d10.getOwnerUid());
				assertEquals(user, d10.getOwnerUserName());
				assertEquals("users", d10.getOwnerGroupName());
				assertEquals("00", d10.getUstarVersion());
				assertEquals(1, d10.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d10.getPaxVariables().get("mtime")));
				assertEquals(1, d10.getChildEntries().size());

				PaxDirectoryEntry d10d20 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d10/d20"));
				assertSame(PaxDirectoryEntry.class, d10d20.getClass());
				assertSame(d10d20, d10.getChildEntries().get("d20"));
				assertSame(d10, d10d20.getParent());
				assertEquals(new AbsoluteLocation("/d10/d20"), d10d20.getLocation());
				assertEquals("d20", d10d20.getName());
				assertEquals(UnixEntityMode.forCode(0755), d10d20.getEntityMode());
				assertWithinLast30Seconds(d10d20.getLastModificationTime());
				assertEquals(0, d10d20.getOwnerGid());
				assertEquals(0, d10d20.getOwnerUid());
				assertEquals(user, d10d20.getOwnerUserName());
				assertEquals("users", d10d20.getOwnerGroupName());
				assertEquals("00", d10d20.getUstarVersion());
				assertEquals(1, d10d20.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d10d20.getPaxVariables().get("mtime")));
				assertEquals(0, d10d20.getChildEntries().size());

				PaxDirectoryEntry d3 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d3"));
				assertSame(PaxDirectoryEntry.class, d3.getClass());
				assertSame(d3, root.getChildEntries().get("d3"));
				assertSame(root, d3.getParent());
				assertEquals(new AbsoluteLocation("/d3"), d3.getLocation());
				assertEquals("d3", d3.getName());
				assertEquals(UnixEntityMode.forCode(0755), d3.getEntityMode());
				assertWithinLast30Seconds(d3.getLastModificationTime());
				assertEquals(0, d3.getOwnerGid());
				assertEquals(0, d3.getOwnerUid());
				assertEquals(user, d3.getOwnerUserName());
				assertEquals("users", d3.getOwnerGroupName());
				assertEquals("00", d3.getUstarVersion());
				assertEquals(1, d3.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d3.getPaxVariables().get("mtime")));
				assertEquals(2, d3.getChildEntries().size());

				PaxFileEntry d3f10 = (PaxFileEntry) tf.get(new AbsoluteLocation("/d3/f10.txt"));
				assertSame(PaxFileEntry.class, d3f10.getClass());
				assertSame(d3f10, d3.getChildEntries().get("f10.txt"));
				assertSame(d3, d3f10.getParent());
				assertEquals(new AbsoluteLocation("/d3/f10.txt"), d3f10.getLocation());
				assertEquals("f10.txt", d3f10.getName());
				assertEquals(UnixEntityMode.forCode(0644), d3f10.getEntityMode());
				assertWithinLast30Seconds(d3f10.getLastModificationTime());
				assertEquals(0, d3f10.getOwnerGid());
				assertEquals(0, d3f10.getOwnerUid());
				assertEquals(user, d3f10.getOwnerUserName());
				assertEquals("users", d3f10.getOwnerGroupName());
				assertEquals("00", d3f10.getUstarVersion());
				assertEquals(1, d3f10.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d3f10.getPaxVariables().get("mtime")));
				assertEquals(19, d3f10.getSize());
				assertEquals("Contents of f10.txt", Files.readTextFile(d3f10));

				PaxDirectoryEntry d3d10 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d3/d10"));
				assertSame(PaxDirectoryEntry.class, d3d10.getClass());
				assertSame(d3d10, d3.getChildEntries().get("d10"));
				assertSame(d3, d3d10.getParent());
				assertEquals(new AbsoluteLocation("/d3/d10"), d3d10.getLocation());
				assertEquals("d10", d3d10.getName());
				assertEquals(UnixEntityMode.forCode(0777), d3d10.getEntityMode());
				assertWithinLast30Seconds(d3d10.getLastModificationTime());
				assertEquals(0, d3d10.getOwnerGid());
				assertEquals(0, d3d10.getOwnerUid());
				assertEquals(user, d3d10.getOwnerUserName());
				assertEquals("users", d3d10.getOwnerGroupName());
				assertEquals("00", d3d10.getUstarVersion());
				assertEquals(1, d3d10.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d3d10.getPaxVariables().get("mtime")));
				assertEquals(1, d3d10.getChildEntries().size());

				PaxDirectoryEntry d3d10d20 = (PaxDirectoryEntry) tf.get(new AbsoluteLocation("/d3/d10/d20"));
				assertSame(PaxDirectoryEntry.class, d3d10d20.getClass());
				assertSame(d3d10d20, d3d10.getChildEntries().get("d20"));
				assertSame(d3d10, d3d10d20.getParent());
				assertEquals(new AbsoluteLocation("/d3/d10/d20"), d3d10d20.getLocation());
				assertEquals("d20", d3d10d20.getName());
				assertEquals(UnixEntityMode.forCode(0755), d3d10d20.getEntityMode());
				assertWithinLast30Seconds(d3d10d20.getLastModificationTime());
				assertEquals(0, d3d10d20.getOwnerGid());
				assertEquals(0, d3d10d20.getOwnerUid());
				assertEquals(user, d3d10d20.getOwnerUserName());
				assertEquals("users", d3d10d20.getOwnerGroupName());
				assertEquals("00", d3d10d20.getUstarVersion());
				assertEquals(1, d3d10d20.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(d3d10d20.getPaxVariables().get("mtime")));
				assertEquals(0, d3d10d20.getChildEntries().size());
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
			TestFileSupport.deleteRecursively(dir1);
		}
	}

	@Test
	public void testPaxTarNonAsciiCharacterInPathName()
	{
		String user = System.getProperty("user.name");
		ReadWritableFile raf = createTargetFile();
		try
		{
			// Use CP 1252 when creating the Tar file and Mac Roman when reading it.
			T tb = createTarBuilder(raf, new TarBuilderSettings().setEntryStrategy(new PaxTarEntryStrategy(CP1252)));
			tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of räksmörgås.txt", Charsets.UTF8), "räksmörgås.txt"));
			tb.close();

			TarFile tf = new TarFile(raf, MAC_ROMAN);
			try
			{
				assertEquals(2, tf.size());

				TarDirectoryEntry root = tf.getRootEntry();
				assertHasStandardRootEntry(root);

				// This should work because the Pax entry strategy detected the
				// non-ASCII characters in the file name and created a path
				// header (which always is encoded in UTF-8)
				PaxFileEntry f1 = (PaxFileEntry) tf.get(new AbsoluteLocation("/räksmörgås.txt"));
				assertSame(PaxFileEntry.class, f1.getClass());
				assertSame(f1, root.getChildEntries().get("räksmörgås.txt"));
				assertSame(root, f1.getParent());
				assertEquals(new AbsoluteLocation("/räksmörgås.txt"), f1.getLocation());
				assertEquals("räksmörgås.txt", f1.getName());
				assertEquals(UnixEntityMode.forCode(0644), f1.getEntityMode());
				assertWithinLast30Seconds(f1.getLastModificationTime());
				assertEquals(0, f1.getOwnerGid());
				assertEquals(0, f1.getOwnerUid());
				assertEquals("00", f1.getUstarVersion());
				assertEquals(user, f1.getOwnerUserName());
				assertEquals("users", f1.getOwnerGroupName());
				assertEquals(2, f1.getPaxVariables().size());
				assertWithinLast30Seconds(parsePaxDate(f1.getPaxVariables().get("mtime")));
				assertEquals("räksmörgås.txt", f1.getPaxVariables().get("path"));
				assertEquals(1536, f1.getStartPosOfFileData());
				assertEquals(29, f1.getSize());
				assertEquals("Contents of räksmörgås.txt", Files.readTextFile(f1, Charsets.UTF8));
			}
			finally
			{
				tf.close();
			}
		}
		finally
		{
			raf.delete();
		}
	}
}
