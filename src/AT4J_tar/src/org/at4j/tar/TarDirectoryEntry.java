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

import java.util.Collections;
import java.util.Map;

import org.at4j.archive.ArchiveDirectoryEntry;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This object represents a Unix v7 Tar directory entry.
 * <p>
 * Tar entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarFileEntry
 * @see TarSymbolicLinkEntry
 */
public class TarDirectoryEntry extends TarEntry implements ArchiveDirectoryEntry<TarEntry, TarDirectoryEntry>
{
	private static final UnixEntityMode DEFAULT_ENTITY_MODE = UnixEntityMode.forCode(0755);

	private final Map<String, TarEntry> m_childEntries;

	TarDirectoryEntry(TarEntryHeaderData hd, AbsoluteLocation location, TarEntryCollaborator collaborator, Map<String, TarEntry> childEntries)
	{
		super(hd, location, collaborator);
		// Make unmodifiable
		m_childEntries = Collections.unmodifiableMap(childEntries);
	}

	@Override
	protected UnixEntityMode getDefaultEntityMode()
	{
		return DEFAULT_ENTITY_MODE;
	}

	/**
	 * Get a read only map containing this directory entry's child entries,
	 * keyed under their (file) names.
	 * @return A read only map containing this directory entry's child entries.
	 * This may be empty, but never {@code null}.
	 */
	public Map<String, TarEntry> getChildEntries()
	{
		// Unmodifiable
		return m_childEntries;
	}

	public boolean isEmpty()
	{
		return m_childEntries.isEmpty();
	}
}
