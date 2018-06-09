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

import java.util.Map;

import org.entityfs.el.AbsoluteLocation;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
final class TarFileContents
{
	private final TarDirectoryEntry m_rootEntry;
	private final Map<AbsoluteLocation, TarEntry> m_entryMap;

	public TarFileContents(TarDirectoryEntry rootEntry, Map<AbsoluteLocation, TarEntry> entryMap)
	{
		// Null checks
		rootEntry.getClass();
		entryMap.getClass();

		m_rootEntry = rootEntry;
		m_entryMap = entryMap;
	}

	public TarDirectoryEntry getRootEntry()
	{
		return m_rootEntry;
	}

	public Map<AbsoluteLocation, TarEntry> getEntryMap()
	{
		return m_entryMap;
	}
}
