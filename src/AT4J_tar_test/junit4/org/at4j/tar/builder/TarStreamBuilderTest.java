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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.at4j.comp.bzip2.BZip2ReadableFile;
import org.at4j.comp.bzip2.BZip2WritableFile;
import org.at4j.tar.TarDirectoryEntry;
import org.at4j.tar.TarFile;
import org.at4j.tar.TarFileEntry;
import org.at4j.tar.TarFileParseException;
import org.entityfs.Directory;
import org.entityfs.NamedReadableFile;
import org.entityfs.ReadWritableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.ram.RamFileSystemBuilder;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.ByteArrayReadableFile;
import org.entityfs.util.ByteArrayWritableFile;
import org.entityfs.util.CharSequenceReadableFile;
import org.entityfs.util.Directories;
import org.entityfs.util.Files;
import org.entityfs.util.NamedReadableFileAdapter;
import org.junit.Test;

public class TarStreamBuilderTest extends AbstractTarBuilderTest<TarStreamBuilder>
{
	@Override
	protected TarStreamBuilder createTarBuilder(ReadWritableFile raf, TarBuilderSettings settings)
	{
		return new TarStreamBuilder(raf, settings);
	}

	@Override
	protected void addDataFromStream(TarStreamBuilder tb, InputStream is, AbsoluteLocation l, TarEntrySettings settings)
	{
		// Read the stream to a file first
		try
		{
			ByteArrayWritableFile bawf = new ByteArrayWritableFile();
			try
			{
				OutputStream os = bawf.openForWrite();
				try
				{
					StreamUtil.copyStreams(is, os, 4096);
				}
				finally
				{
					os.close();
				}
			}
			finally
			{
				is.close();
			}

			NamedReadableFile f = new NamedReadableFileAdapter(new ByteArrayReadableFile(bawf.toByteArray()), l.getName());
			if (settings != null)
			{
				tb.add(f, l.getParentLocation(), settings);
			}
			else
			{
				tb.add(f, l.getParentLocation());
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testCreateCompressedArchiveOnStream() throws IOException
	{
		ReadWritableFile raf = createTargetFile();
		try
		{
			Directory dir = Directories.newDirectory(new RamFileSystemBuilder().create().getRootDirectory(), "d");

			OutputStream os = new BZip2WritableFile(raf).openForWrite();
			try
			{
				TarStreamBuilder tb = new TarStreamBuilder(os, new TarBuilderSettings().setEntryStrategy(new V7TarEntryStrategy()));
				tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f1.txt"), "f1.txt"));
				tb.add(dir);
				tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of file without parent dir"), "file_without_parent_dir.txt"), new AbsoluteLocation("/nonexisting_parent_dir_for_file"));
				tb.add(dir, new AbsoluteLocation("/nonexisting_parent_dir_for_directory"));
				tb.add(new NamedReadableFileAdapter(new CharSequenceReadableFile("Contents of f2.txt"), "f2.txt"));
				tb.close();
			}
			finally
			{
				os.close();
			}

			// Try to open the Tar file on the compressed file
			try
			{
				new TarFile(raf);
				fail();
			}
			catch (TarFileParseException e)
			{
				// ok
			}

			// Uncompress the file
			ReadWritableFile ucf = createTargetFile();
			try
			{
				Files.writeFromFile(ucf, new BZip2ReadableFile(raf));
				TarFile tf = new TarFile(ucf);
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
				ucf.delete();
			}
		}
		finally
		{
			raf.delete();
		}
	}
}
