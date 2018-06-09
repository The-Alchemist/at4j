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
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.exception.UnsupportedEntityTypeException;

/**
 * This factory is used to create {@link UnixExternalFileAttributes} instances.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnixExternalFileAttributes
 */
public class UnixExternalFileAttributesFactory implements ZipExternalFileAttributesFactory
{
	/**
	 * This instance has the default settings for this factory (
	 * {@link UnixExternalFileAttributes#DEFAULT_FILE_ATTRIBUTES} for files and
	 * {@link UnixExternalFileAttributes#DEFAULT_DIRECTORY_ATTRIBUTES} for
	 * directories). It may be used instead of creating a new factory instance.
	 */
	public static final UnixExternalFileAttributesFactory DEFAULT_INSTANCE = new UnixExternalFileAttributesFactory(UnixExternalFileAttributes.DEFAULT_FILE_ATTRIBUTES.getEntityMode(), UnixExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES
			.getEntityMode());

	private final UnixExternalFileAttributes m_fileAttributes;
	private final UnixExternalFileAttributes m_directoryAttributes;

	/**
	 * Create a new factory instance.
	 * @param fileMode The entity mode to use for files.
	 * @param directoryMode The entity mode to use for directories.
	 */
	public UnixExternalFileAttributesFactory(UnixEntityMode fileMode, UnixEntityMode directoryMode)
	{
		// UnixEntityMode is immutable
		m_fileAttributes = new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, fileMode);
		m_directoryAttributes = new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, directoryMode);
	}

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.UNIX;
	}

	public UnixExternalFileAttributes create(UnixEntityType entityType, AbsoluteLocation loc, Object entryToZip)
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
			throw new UnsupportedEntityTypeException("Unsupported entity type " + entityType);
		}
	}
}
