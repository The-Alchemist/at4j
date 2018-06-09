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

import java.util.Collections;
import java.util.Map;

import org.at4j.archive.ArchiveDirectoryEntry;
import org.entityfs.el.AbsoluteLocation;

/**
 * This object represents a directory entry in a Zip file. In addition to the
 * properties inherited from {@link ZipEntry}, it contains a collection of child
 * entries.
 * <p>
 * Zip entries are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipFileEntry
 * @see ZipSymbolicLinkEntry
 */
public class ZipDirectoryEntry extends ZipEntry implements ArchiveDirectoryEntry<ZipEntry, ZipDirectoryEntry>
{
	private final Map<String, ZipEntry> m_childEntries;

	/**
	 * Create a new Zip directory entry. This constructor is only used for Zip
	 * directory entries that don't have an actual entry in the Zip file, but
	 * that has child entries anyway.
	 * @param collaborator The parent Zip archive's entry collaborator.
	 * @param loc The directory entry's location in the Zip archive.
	 * @param childEntries A map containing this entry's child entries, with the
	 * entries' names as keys. This may be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public ZipDirectoryEntry(ZipEntryCollaborator collaborator, AbsoluteLocation loc, Map<String, ZipEntry> childEntries)
	{
		super(collaborator, loc);
		// Unmodifiable.
		m_childEntries = childEntries != null ? Collections.unmodifiableMap(childEntries) : Collections.EMPTY_MAP;
	}

	/**
	 * Create a new Zip directory entry.
	 * @param collaborator The parent Zip archive's entry collaborator.
	 * @param zecd Data parsed from the Zip entry's record in the central
	 * directory.
	 * @param zeld Data parsed from the Zip entry's local header.
	 * @param childEntries A map containing this entry's child entries, with the
	 * entries' names as keys. This may be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public ZipDirectoryEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld, Map<String, ZipEntry> childEntries)
	{
		super(collaborator, zecd, zeld);
		// Unmodifiable
		m_childEntries = childEntries != null ? Collections.unmodifiableMap(childEntries) : Collections.EMPTY_MAP;
	}

	public Map<String, ZipEntry> getChildEntries()
	{
		// This collection is unmodifiable
		return m_childEntries;
	}

	public boolean isEmpty()
	{
		return m_childEntries.isEmpty();
	}

	@Override
	public String toString()
	{
		return getName() + " (d)";
	}
}
