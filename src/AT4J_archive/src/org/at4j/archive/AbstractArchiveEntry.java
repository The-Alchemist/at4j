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
package org.at4j.archive;

import org.entityfs.el.AbsoluteLocation;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.lock.DummyLock;
import org.entityfs.lock.EntityLock;

/**
 * Abstract base class for archive entry implementations.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <T> The type of entries in this archive.
 * @param <U> The type of directory entries in this archive.
 */
public abstract class AbstractArchiveEntry<T extends ArchiveEntry<T, U>, U extends ArchiveDirectoryEntry<T, U>> implements ArchiveEntry<T, U>
{
	private final AbsoluteLocation m_location;
	private final ArchiveEntryCollaborator<T, U> m_collaborator;

	/**
	 * Create a new archive entry.
	 * @param loc The entry's location in the archive.
	 * @param collaborator Collaborator object that gives access to the archive.
	 */
	protected AbstractArchiveEntry(AbsoluteLocation loc, ArchiveEntryCollaborator<T, U> collaborator)
	{
		// Null checks
		loc.getClass();
		collaborator.getClass();

		m_location = loc;
		m_collaborator = collaborator;
	}

	public AbsoluteLocation getLocation()
	{
		return m_location;
	}

	protected ArchiveEntryCollaborator<T, U> getCollaborator()
	{
		return m_collaborator;
	}

	/**
	 * Since archive objects are read only, this method only returns a dummy
	 * lock.
	 * @return A dummy lock.
	 */
	public EntityLock getReadLock()
	{
		return DummyLock.INSTANCE;
	}

	/**
	 * This method always returns {@code true}.
	 * @return {@code true}, always.
	 */
	public boolean isReadLockedByCurrentThread() throws IllegalStateException
	{
		return true;
	}

	/**
	 * Since archive objects are read only, this method only returns a dummy
	 * lock.
	 * @return A dummy lock.
	 */
	public EntityLock lockForReading() throws LockTimeoutException
	{
		return DummyLock.INSTANCE;
	}

	/**
	 * Get the entry's file name.
	 * @return The entry's file name.
	 */
	public String getName()
	{
		return m_location.getName();
	}

	/**
	 * Get the parent directory entry for this archive entry.
	 * @return The entry's parent directory entry. If the entry is the root
	 * entry, this method returns {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public U getParent()
	{
		if (getLocation().equals(AbsoluteLocation.ROOT_DIR))
		{
			return null;
		}
		else
		{
			return (U) m_collaborator.getEntry(getLocation().getParentLocation());
		}
	}
}
