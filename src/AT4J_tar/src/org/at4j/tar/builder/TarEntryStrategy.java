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

import java.io.InputStream;
import java.util.Date;

import org.at4j.archive.builder.ArchiveEntryAddException;
import org.entityfs.DataSink;
import org.entityfs.FileSystem;
import org.entityfs.RandomAccess;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This interface defines a strategy for how Tar entries are written to a tar
 * file. Different implementations of this interface create Tar files formatted
 * in different ways.
 * <p>
 * Implementations of this interface is used by the {@link TarBuilder}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarBuilder
 */
public interface TarEntryStrategy
{
	/**
	 * Write a file entry to the Tar file.
	 * @param out The {@code DataSink} to write to. This may be a
	 * {@link RandomAccess} object. When this method is called, the {@code
	 * DataSink} is positioned at the start of the entry to write. If this
	 * method returns successfully, the {@code DataSink} should be positioned at
	 * the next block boundary after the written entry (the Tar file is divided
	 * into 512-byte blocks).
	 * @param f The file to add to the archive. If this file is in a locking
	 * {@link FileSystem}, it is locked for reading by the caller before this
	 * method is called.
	 * @param location The absolute location of the entry in the Tar archive.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the file entity was last modified.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file entry cannot be added for
	 * some other reason than an I/O error.
	 */
	void writeFile(DataSink out, ReadableFile f, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException;

	/**
	 * Write a file entry containing all data that can be read from the stream
	 * to the Tar file.
	 * @param out The {@code RandomAccess} to write to. When this method is
	 * called, the {@code RandomAccess} is positioned at the start of the entry
	 * to write. If this method returns successfully, the {@code RandomAccess}
	 * should be positioned at the next block boundary after the written entry
	 * (the Tar file is divided into 512-byte blocks).
	 * @param is The stream containing the file's data.
	 * @param location The absolute location of the entry in the Tar archive.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the file entity was last modified.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file entry cannot be added for
	 * some other reason than an I/O error.
	 */
	void writeFileFromStream(RandomAccess out, InputStream is, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException;

	/**
	 * Write a directory entry to the Tar file.
	 * @param out The {@code DataSink} to write to. This may be a
	 * {@link RandomAccess} object. When this method is called, the {@code
	 * DataSink} is positioned at the start of the entry to write. If this
	 * method returns successfully, the {@code DataSink} should be positioned at
	 * the next block boundary after the written entry (the Tar file is divided
	 * into 512-byte blocks).
	 * @param d The directory to add to the Tar file.
	 * @param location The absolute location of the entry in the Tar archive.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the directory entity was last modified.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the directory entry cannot be added
	 * for some other reason than an I/O error.
	 */
	void writeDirectory(DataSink out, DirectoryAdapter<?> d, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException;
}
