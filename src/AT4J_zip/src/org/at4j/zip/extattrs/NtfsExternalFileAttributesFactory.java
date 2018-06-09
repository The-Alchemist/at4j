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
 * This factory is used for creating {@link NtfsExternalFileAttributes} objects.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class NtfsExternalFileAttributesFactory implements ZipExternalFileAttributesFactory
{
	/**
	 * This instance has the default settings for file and directory attributes
	 * ({@link NtfsExternalFileAttributes#DEFAULT_FILE_ATTRIBUTES} and
	 * {@link NtfsExternalFileAttributes#DEFAULT_DIRECTORY_ATTRIBUTES},
	 * respectively).
	 */
	public static final NtfsExternalFileAttributesFactory DEFAULT_INSTANCE = new NtfsExternalFileAttributesFactory(NtfsExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES, NtfsExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES);

	private final NtfsExternalFileAttributes m_fileAttributes;
	private final NtfsExternalFileAttributes m_directoryAttributes;

	/**
	 * Create a new factory.
	 * @param fileAttrs The attributes that will be used for files.
	 * @param dirAttrs The attributes that will be used for directories.
	 */
	public NtfsExternalFileAttributesFactory(NtfsExternalFileAttributes fileAttrs, NtfsExternalFileAttributes dirAttrs)
	{
		// Immutable
		m_fileAttributes = fileAttrs;
		m_directoryAttributes = dirAttrs;
	}

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.WINDOWS_NTFS;
	}

	public NtfsExternalFileAttributes create(UnixEntityType entityType, AbsoluteLocation loc, Object entryToZip)
	{
		if (entityType == UnixEntityType.REGULAR_FILE)
		{
			return m_fileAttributes;
		}
		else if (entityType == UnixEntityType.DIRECTORY)
		{
			return m_directoryAttributes;
		}
		else
		{
			throw new UnsupportedEntityTypeException("The entity type " + entityType + " is not supported");
		}
	}
}
