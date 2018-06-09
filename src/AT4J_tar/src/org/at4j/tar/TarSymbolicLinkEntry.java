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

import org.at4j.archive.ArchiveSymbolicLinkEntry;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.el.EntityLocation;
import org.entityfs.el.RelativeLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This object represents a Unix v7 Tar symbolic link entry.
 * <p>
 * Tar entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarDirectoryEntry
 * @see TarFileEntry
 */
public class TarSymbolicLinkEntry extends TarEntry implements ArchiveSymbolicLinkEntry<TarEntry, TarDirectoryEntry>
{
	private static final UnixEntityMode DEFAULT_ENTITY_MODE = UnixEntityMode.forCode(0777);

	private final EntityLocation<?> m_linkTarget;

	TarSymbolicLinkEntry(TarEntryHeaderData hd, TarEntryCollaborator collaborator)
	{
		super(hd, hd.getLocation(), collaborator);

		m_linkTarget = hd.getLinkName().startsWith("/") ? new AbsoluteLocation(hd.getLinkName()) : new RelativeLocation(hd.getLinkName());
	}

	@Override
	protected UnixEntityMode getDefaultEntityMode()
	{
		return DEFAULT_ENTITY_MODE;
	}

	/**
	 * Get the target of this symbolic link. This is either an
	 * {@link AbsoluteLocation} or a {@link RelativeLocation} relative to the
	 * symbolic link's parent directory.
	 * @return The target of this symbolic link.
	 */
	public EntityLocation<?> getLinkTarget()
	{
		return m_linkTarget;
	}
}
