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

import java.util.Map;

import org.entityfs.el.AbsoluteLocation;

/**
 * This object contains the root entry of a Zip file and a map with the Zip file
 * contents. It is returned by
 * {@link ZipFileParser#parse(ZipEntryCollaborator, org.entityfs.RandomAccess, java.nio.charset.Charset, java.nio.charset.Charset)}
 * .
 * <p>
 * This is part of the {@link ZipFile} implementations. Clients should not have
 * to bother with this object.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipFileContents
{
	private final ZipDirectoryEntry m_rootEntry;
	private final Map<AbsoluteLocation, ZipEntry> m_entryMap;
	private final String m_comment;

	public ZipFileContents(ZipDirectoryEntry rootEntry, Map<AbsoluteLocation, ZipEntry> entryMap, String comment)
	{
		// Null checks
		rootEntry.getClass();
		entryMap.getClass();
		comment.getClass();

		m_rootEntry = rootEntry;
		m_entryMap = entryMap;
		m_comment = comment;
	}

	public ZipDirectoryEntry getRootEntry()
	{
		return m_rootEntry;
	}

	public Map<AbsoluteLocation, ZipEntry> getEntryMap()
	{
		return m_entryMap;
	}

	public String getComment()
	{
		return m_comment;
	}
}
