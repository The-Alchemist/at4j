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
package org.at4j.zip.extattrs;

import org.at4j.zip.ZipVersionMadeBy;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.exception.UnsupportedEntityTypeException;

/**
 * This interface defines a factory object that is used to create
 * {@link ZipExternalFileAttributes} objects for the external file attributes of
 * Zip entries that are created by a specific versions of the Zip software such
 * as "MSDOS" or "Unix".
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipExternalFileAttributesParserRegistry
 */
public interface ZipExternalFileAttributesFactory
{
	/**
	 * Get the Zip version that is used to make the kind of external file
	 * attributes that are created by this factory.
	 * @return The version.
	 */
	ZipVersionMadeBy getVersionMadeBy();

	/**
	 * Create external file attributes for the entry to Zip.
	 * @param entityType The type of the entry (file or directory).
	 * @param loc The absolute location of the entry in the Zip archive.
	 * @param entryToZip The entry to Zip. This may be a
	 * {@link org.entityfs.ReadableFile}, a {@link org.entityfs.DirectoryView},
	 * a {@link java.io.File} (directory) or an {@link java.io.InputStream}. If
	 * the object is {@link org.entityfs.lock.ReadLockable} ({@code
	 * ReadableFile} and {@code DirectoryView}), it is locked for reading when
	 * this method is called. If it is an input stream, this method may
	 * <i>not</i> read any data from it.
	 * @return External file attributes for the Zip entry.
	 * @throws UnsupportedEntityTypeException If the entity type is not
	 * supported.
	 */
	ZipExternalFileAttributes create(UnixEntityType entityType, AbsoluteLocation loc, Object entryToZip) throws UnsupportedEntityTypeException;
}
