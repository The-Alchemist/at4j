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

/**
 * This object represents a symbolic link entry created with a POSIX-1.2001
 * compatible version of tar.
 * <p>
 * Tar entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see PaxDirectoryEntry
 * @see PaxFileEntry
 */
public class PaxSymbolicLinkEntry extends UstarSymbolicLinkEntry implements PaxEntry
{
	private final Map<String, String> m_paxVariables;

	PaxSymbolicLinkEntry(TarEntryHeaderData hd, TarEntryCollaborator collaborator)
	{
		super(hd, collaborator);

		m_paxVariables = Collections.unmodifiableMap(hd.getVariables());
	}

	public Map<String, String> getPaxVariables()
	{
		// Unmodifiable
		return m_paxVariables;
	}
}
