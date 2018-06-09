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

import org.at4j.archive.ArchiveEntry;

/**
 * This interface defines the properties that an POSIX 1003.1-1988 (ustar) entry
 * has in addition to the properties inherited from {@link TarEntry}.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface UstarEntry extends ArchiveEntry<TarEntry, TarDirectoryEntry>
{
	/**
	 * Get the Ustar version used to create this entry. This is often {@code 00}
	 * or an empty string.
	 */
	String getUstarVersion();

	/**
	 * Get the user name for the owner of the file system entity that was added
	 * to the Tar archive to make this Tar entry.
	 * <p>
	 * Tar software should let this name take precedence over the numerical
	 * owner id from {@link TarEntry#getOwnerUid()}.
	 * @return The owner's user name.
	 * @see #getOwnerGroupName()
	 */
	String getOwnerUserName();

	/**
	 * Get the name of the group that owned the file system entity that was
	 * added to the Tar archive to make this Tar entry.
	 * <p>
	 * Tar software should let this name take precedence over the numerical
	 * group id from {@link TarEntry#getOwnerGid()}.
	 * @return The owner group name.
	 * @see #getOwnerUserName()
	 */
	String getOwnerGroupName();
}
