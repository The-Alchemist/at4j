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
package org.at4j.zip.ef;

import java.util.Date;

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.builder.ZipBuilderConfiguration;

/**
 * This is the older InfoZip Unix extra field. The central header version
 * contains the last access and modification times. The local header version
 * contains that and information on the file owner UID and GID.
 * @author Karl Gustafsson
 * @since 1.0
 * @see InfoZipUnixExtraFieldFactory
 * @see NewInfoZipUnixExtraField
 */
public class InfoZipUnixExtraField implements ZipEntryExtraField
{
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0x5855);

	private final boolean m_inLocalHeader;
	private final Date m_lastAccessTime;
	private final Date m_lastModificationTime;
	// This is only set in the local header version
	private final UnsignedShort m_uid;
	// This is only set in the local header version
	private final UnsignedShort m_gid;

	/**
	 * Create a central header version of this object.
	 * @param lastAccessTime The Zip entry's last access time.
	 * @param lastModificationTime The Zip entry's last modification time.
	 */
	public InfoZipUnixExtraField(Date lastAccessTime, Date lastModificationTime)
	{
		// Null checks
		lastAccessTime.getClass();
		lastModificationTime.getClass();

		m_inLocalHeader = false;
		// Defensive copy
		m_lastAccessTime = (Date) lastAccessTime.clone();
		m_lastModificationTime = (Date) lastModificationTime.clone();
		m_uid = null;
		m_gid = null;
	}

	/**
	 * Create a local header version of this object.
	 * @param lastAccessTime The Zip entry's last access time.
	 * @param lastModificationTime The Zip entry's last modification time.
	 * @param uid The Zip entry's owner's user id.
	 * @param gid The Zip entry's owner's group id.
	 */
	public InfoZipUnixExtraField(Date lastAccessTime, Date lastModificationTime, UnsignedShort uid, UnsignedShort gid)
	{
		// Null checks
		lastAccessTime.getClass();
		lastModificationTime.getClass();
		uid.getClass();
		gid.getClass();

		m_inLocalHeader = true;
		// Defensive copy
		m_lastAccessTime = (Date) lastAccessTime.clone();
		m_lastModificationTime = (Date) lastModificationTime.clone();
		m_uid = uid;
		m_gid = gid;
	}

	public boolean isInLocalHeader()
	{
		return m_inLocalHeader;
	}

	/**
	 * Get the owner user id for the Zip entry.
	 * @return The owner user id, or {@code null} if this is the central header
	 * version of this object.
	 */
	public UnsignedShort getUid()
	{
		return m_uid;
	}

	/**
	 * Get the owner group id for the Zip entry.
	 * @return The owner group id, or {@code null} if this is the central header
	 * version of this object.
	 */
	public UnsignedShort getGid()
	{
		return m_gid;
	}

	/**
	 * Get the Zip entry's last access time.
	 * @return The Zip entry's last access time.
	 */
	public Date getLastAccessTime()
	{
		// Return a defensive copy
		return (Date) m_lastAccessTime.clone();
	}

	/**
	 * Get the Zip entry's last modification time.
	 * @return The Zip entry's last modification time.
	 */
	public Date getLastModificationTime()
	{
		// Return a defensive copy
		return (Date) m_lastModificationTime.clone();
	}

	@Override
	public String toString()
	{
		return "Unix - uid: " + (m_uid != null ? m_uid : "<null>") + ", gid: " + (m_gid != null ? m_gid : "<null>") + ", last access time: " + m_lastAccessTime + ", last modification time: " + m_lastModificationTime;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof InfoZipUnixExtraField))
		{
			InfoZipUnixExtraField o2 = (InfoZipUnixExtraField) o;
			if (m_inLocalHeader ^ o2.m_inLocalHeader)
			{
				return false;
			}

			if (!(m_lastAccessTime.equals(o2.m_lastAccessTime) && m_lastModificationTime.equals(o2.m_lastModificationTime)))
			{
				return false;
			}

			if (m_inLocalHeader && o2.m_inLocalHeader)
			{
				return m_uid.equals(o2.m_uid) && m_gid.equals(o2.m_gid);
			}
			else
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int res = m_lastAccessTime.hashCode();
		res = 17 * res + m_lastModificationTime.hashCode();
		return m_inLocalHeader ? 17 * res + m_uid.intValue() << 16 + m_gid.intValue() : res;
	}

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		byte[] res;
		if (m_inLocalHeader)
		{
			res = new byte[12];

			byte[] uid = m_uid.getBigEndianByteArray();
			res[8] = uid[0];
			res[9] = uid[1];

			byte[] gid = m_gid.getBigEndianByteArray();
			res[10] = gid[0];
			res[11] = gid[1];
		}
		else
		{
			res = new byte[8];
		}
		byte[] lastAccessTime = UnsignedInteger.valueOf(m_lastAccessTime.getTime() / 1000).getBigEndianByteArray();
		res[0] = lastAccessTime[0];
		res[1] = lastAccessTime[1];
		res[2] = lastAccessTime[2];
		res[3] = lastAccessTime[3];

		byte[] lastModificationTime = UnsignedInteger.valueOf(m_lastModificationTime.getTime() / 1000).getBigEndianByteArray();
		res[4] = lastModificationTime[0];
		res[5] = lastModificationTime[1];
		res[6] = lastModificationTime[2];
		res[7] = lastModificationTime[3];

		return res;
	}
}
