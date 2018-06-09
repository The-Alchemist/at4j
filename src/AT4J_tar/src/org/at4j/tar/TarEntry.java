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

import java.util.Date;

import org.at4j.archive.AbstractArchiveEntry;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This is the abstract base class for a Tar entry. It contains the metadata
 * that is present in all Tar entries regardless of their types and the version
 * of the Tar software that created them.
 * <p>
 * Tar entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 */
public abstract class TarEntry extends AbstractArchiveEntry<TarEntry, TarDirectoryEntry>
{
	private final UnixEntityMode m_entityMode;
	private final int m_ownerUid;
	private final int m_ownerGid;
	private final Date m_lastModificationTime;
	private final int m_checksum;

	TarEntry(TarEntryHeaderData hd, AbsoluteLocation location, TarEntryCollaborator collaborator)
	{
		super(location, collaborator);
		if (hd != null)
		{
			m_entityMode = hd.getMode();
			m_ownerUid = hd.getOwnerUid();
			m_ownerGid = hd.getOwnerGid();
			m_lastModificationTime = hd.getLastModificationTime();
			m_checksum = hd.getChecksum();
		}
		else
		{
			m_entityMode = getDefaultEntityMode();
			m_ownerUid = 0;
			m_ownerGid = 0;
			m_lastModificationTime = new Date();
			m_checksum = 0;
		}
	}

	/**
	 * Subclasses implement this to return the default Unix entity mode if none
	 * is set in the Tar file. This is called from the constructor.
	 * @return The default entity mode for the entry type.
	 */
	protected abstract UnixEntityMode getDefaultEntityMode();

	/**
	 * Get the Unix entity mode for the entry. The Unix entity mode is the
	 * permissions settings for the entry.
	 * @return The Unix entity mode for the entry.
	 */
	public UnixEntityMode getEntityMode()
	{
		return m_entityMode;
	}

	/**
	 * Get the owner user id for the entry. This property allows a value range
	 * between {@code 0} and {@code 2097151}. The actual range of the property
	 * depends on the Tar software and the operating system platform used when
	 * creating the archive.
	 * <p>
	 * Some entry versions support expressing the entry owner's user name in
	 * text. Software that interpret Tar file should let that text value take
	 * precedence over this numerical value.
	 * @return The owner user id for the entry.
	 * @see #getOwnerGid()
	 */
	public int getOwnerUid()
	{
		return m_ownerUid;
	}

	/**
	 * Get the owner group id for the entry. This property allows a value range
	 * between {@code 0} and {@code 2097151}. The actual range of the property
	 * depends on the Tar software and the operating system platform used when
	 * creating the archive.
	 * <p>
	 * Some entry versions support expressing the entry owner's group name in
	 * text. Software that interpret Tar file should let that text value take
	 * precedence over this numerical value.
	 * @return The owner group id for the entry.
	 * @see #getOwnerUid()
	 */
	public int getOwnerGid()
	{
		return m_ownerGid;
	}

	/**
	 * Get the time of last modification for the file system entity that was
	 * added to the Tar file to create this Tar entry.
	 * <p>
	 * The time zone for the timestamp is UTC.
	 * @return The last modification time.
	 */
	public Date getLastModificationTime()
	{
		// Return a defensive copy
		return (Date) m_lastModificationTime.clone();
	}

	/**
	 * Get the entry's checksum. The checksum is calculated by adding together
	 * the values of all individual bytes in the Tar entry header, with the
	 * checksum field taken to be eight spaces.
	 * <p>
	 * This is a value in the range of {@code 0} and {@code 130560}.
	 * @return The Tar entry's checksum.
	 */
	public int getChecksum()
	{
		return m_checksum;
	}
}
