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

/**
 * This object represents a symbolic link entry created with a POSIX-1.1988
 * (ustar) compatible version of tar.
 * <p>
 * Tar entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UstarDirectoryEntry
 * @see UstarFileEntry
 */
public class UstarSymbolicLinkEntry extends TarSymbolicLinkEntry implements UstarEntry
{
	private final String m_ustarVersion;
	private final String m_ownerUserName;
	private final String m_ownerGroupName;

	UstarSymbolicLinkEntry(TarEntryHeaderData hd, TarEntryCollaborator collaborator)
	{
		super(hd, collaborator);
		m_ustarVersion = hd.getUstarVersion();
		m_ownerUserName = hd.getOwnerUserName();
		m_ownerGroupName = hd.getOwnerGroupName();
	}

	public String getUstarVersion()
	{
		return m_ustarVersion;
	}

	public String getOwnerUserName()
	{
		return m_ownerUserName;
	}

	public String getOwnerGroupName()
	{
		return m_ownerGroupName;
	}
}
