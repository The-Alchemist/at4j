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
package org.at4j.archive.builder;

import org.entityfs.EntityType;
import org.entityfs.el.AbsoluteLocation;

/**
 * This object contains data about an entry that is about to be added to an
 * archive. It is used by the different filters that can be used with an
 * {@link ArchiveBuilder}.
 * <p>
 * Instances of this class is immutable.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class EntryToArchive
{
	private final AbsoluteLocation m_location;
	private final EntityType m_entityType;
	private final Object m_entryToArchive;

	/**
	 * Create a new configuration object for an entry that are to be added to an
	 * archive.
	 * @param loc The entry's location in the archive.
	 * @param entType The entry's type.
	 * @param entryToArchive The entry to archive. This may be a
	 * {@link org.entityfs.ReadableFile}, a {@link org.entityfs.DirectoryView},
	 * a {@link java.io.File} or an {@link java.io.InputStream}. In the latter
	 * case, this method may <i>not</i> read any data from the stream. If the
	 * entity is {@link org.entityfs.lock.ReadLockable} (which {@code
	 * ReadableFile} and {@code DirectoryView} is), it is locked for reading
	 * while this object is used.
	 */
	public EntryToArchive(AbsoluteLocation loc, EntityType entType, Object entryToArchive)
	{
		// Null checks
		loc.getClass();
		entType.getClass();
		entryToArchive.getClass();

		m_location = loc;
		m_entityType = entType;
		m_entryToArchive = entryToArchive;
	}

	/**
	 * Get the location of the entry in the archive.
	 * @return The location of the entry in the archive.
	 */
	public AbsoluteLocation getLocation()
	{
		return m_location;
	}

	/**
	 * Get the entry type.
	 * @return The entry type.
	 */
	public EntityType getEntityType()
	{
		return m_entityType;
	}

	/**
	 * Get the entry to archive. This may be a {@link org.entityfs.ReadableFile}
	 * , a {@link org.entityfs.DirectoryView}, a {@link java.io.File} or an
	 * {@link java.io.InputStream}. In the latter case, this method may
	 * <i>not</i> read any data from the stream. If the entity is
	 * {@link org.entityfs.lock.ReadLockable} (which {@code ReadableFile} and
	 * {@code DirectoryView} is), it is locked for reading when this method is
	 * called.
	 * @return The entry to archive.
	 */
	public Object getEntryToArchive()
	{
		return m_entryToArchive;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof EntryToArchive))
		{
			EntryToArchive o2 = (EntryToArchive) o;
			return m_location.equals(o2.m_location) && (m_entityType == o2.m_entityType) && m_entryToArchive.equals(o2.m_entryToArchive);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int res = m_entityType.hashCode();
		res = m_location.hashCode() + 31 * res;
		return m_entryToArchive.hashCode() + 31 * res;
	}
}
