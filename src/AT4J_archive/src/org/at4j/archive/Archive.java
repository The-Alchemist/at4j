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
package org.at4j.archive;

import java.util.Map;

import org.entityfs.el.AbsoluteLocation;

/**
 * This interface defines an archive file. The archive file is represented as a
 * read only {@code Map<AbsoluteLocation, ArchiveEntry>}, i.e. a map containing
 * the archive's {@link ArchiveEntry}:s keyed under their absolute locations in
 * the archive.
 * <p>
 * An archive is opened by creating a new archive object on an archive file.
 * While the archive is opened, it keeps a read lock on the file (if the file is
 * {@link org.entityfs.lock.ReadLockable}), and it opens and closes new streams
 * and {@link org.entityfs.RandomAccess} objects on it as needed. When
 * {@link #close()} is called on an archive object, the read lock is released
 * and all open streams on the archive are closed. After the archive has been
 * closed, all of its methods throw {@link IllegalStateException}.
 * <p>
 * Archive objects are <i>not</i> safe to use concurrently from several threads
 * without external synchronization. If the archive file is in a locking
 * {@link org.entityfs.FileSystem}, all threads other than the thread creating
 * the archive object, manually have to acquire an
 * {@link org.entityfs.lock.EntityLock} for reading the archive file before
 * using any archive methods. (This requires that the file system locking
 * strategy permits several read locks on the same entity.) See the EntityFS
 * documentation for details on file locking.
 * <p>
 * The {@link org.at4j.util.archive.ArchiveExtractor} can be used to extract all
 * entries from an archive.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <T> The type of entries in this archive.
 * @param <U> The type of directory entries in this archive.
 * @see org.at4j.archive.ArchiveEntry
 * @see org.at4j.archive.builder.ArchiveBuilder
 */
public interface Archive<T extends ArchiveEntry<T, U>, U extends ArchiveDirectoryEntry<T, U>> extends Map<AbsoluteLocation, T>
{
	/**
	 * Get the archive's root directory.
	 * @return The archive's root directory.
	 * @throws IllegalStateException If the archive has been closed.
	 */
	U getRootEntry() throws IllegalStateException;

	/**
	 * Close this archive and release all of its resources. After calling this
	 * method, all of the archive's method will throw an
	 * {@link IllegalStateException}.
	 * <p>
	 * It is safe to call this method more than once on an archive.
	 */
	void close();
}
