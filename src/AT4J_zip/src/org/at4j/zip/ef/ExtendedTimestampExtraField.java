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
 * This {@link ZipEntryExtraField} stores zero or more of a Zip entry's last
 * modification, last access and original creation times, specified as
 * {@link Date} values in the UTC time zone.
 * <p>
 * If this field is from the central header, it only contains the last
 * modification time or no time at all.
 * <p>
 * Instances of this object are immutable.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ExtendedTimestampExtraField implements ZipEntryExtraField
{
	/**
	 * The code that is used to identify this extra field in a Zip file.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0x5455);

	private final boolean m_inLocalHeader;
	private final Date m_lastModificationTime;
	private final Date m_lastAccessTime;
	private final Date m_originalCreationTime;

	/**
	 * Create a new {@code UniversalTimeExtraField} object.
	 * @param inLocalHeader Is this extra field from the Zip entry's local
	 * header ({@code true}) or from the Zip file's central header ({@code
	 * false})?
	 * @param lastModificationTime The last modification time of the entity in
	 * the Zip entry. May be {@code null}.
	 * @param lastAccessTime The last access time of the entity in the Zip
	 * entry. May be {@code null}.
	 * @param originalCreationTime The original creation of the entity in the
	 * Zip entry. May be {@code null}.
	 */
	public ExtendedTimestampExtraField(boolean inLocalHeader, Date lastModificationTime, Date lastAccessTime, Date originalCreationTime)
	{
		m_inLocalHeader = inLocalHeader;
		// Defensive copies
		m_lastModificationTime = lastModificationTime != null ? new Date(lastModificationTime.getTime()) : null;
		m_lastAccessTime = lastAccessTime != null ? new Date(lastAccessTime.getTime()) : null;
		m_originalCreationTime = originalCreationTime != null ? new Date(originalCreationTime.getTime()) : null;
	}

	public boolean isInLocalHeader()
	{
		return m_inLocalHeader;
	}

	/**
	 * Get the time of last modification for the entity in the Zip entry, if
	 * set.
	 * @return The time of last modification for the entity in the Zip entry, in
	 * the UTC time zone, or {@code null} if this property was not set in the
	 * extra field.
	 */
	public Date getLastModified()
	{
		// Defensive copy
		return m_lastModificationTime != null ? new Date(m_lastModificationTime.getTime()) : null;
	}

	/**
	 * Get the time when the entity in the Zip entry was last accessed, if set.
	 * <p>
	 * If this extra field is from the Zip file's central header, this method
	 * always returns {@code null}.
	 * @return The last access time for the entity in the Zip entry, in the UTC
	 * time zone, or {@code null} if this property was not set in the extra
	 * field.
	 */
	public Date getLastAccessed()
	{
		// Defensive copy
		return m_lastAccessTime != null ? new Date(m_lastAccessTime.getTime()) : null;
	}

	/**
	 * Get the time when the entity in the Zip entry was originally created, if
	 * set.
	 * <p>
	 * If this extra field is from the Zip file's central header, this method
	 * always returns {@code null}.
	 * @return The original creation time for the entity in the Zip entry, in
	 * the UTC time zone, or {@code null} if this property was not set in the
	 * extra field.
	 */
	public Date getOriginalCreationTime()
	{
		// Defensive copy
		return m_originalCreationTime != null ? new Date(m_originalCreationTime.getTime()): null;
	}

	private boolean datesEqual(Date d1, Date d2)
	{
		return d1 != null ? d1.equals(d2) : d2 == null;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof ExtendedTimestampExtraField))
		{
			ExtendedTimestampExtraField o2 = (ExtendedTimestampExtraField) o;
			return (m_inLocalHeader == o2.isInLocalHeader()) && datesEqual(m_lastModificationTime, o2.m_lastModificationTime) && datesEqual(m_lastAccessTime, o2.m_lastAccessTime)
					&& datesEqual(m_originalCreationTime, o2.m_originalCreationTime);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int res = m_inLocalHeader ? 1 : 0;
		res = 17 * res + (m_lastModificationTime != null ? m_lastModificationTime.hashCode() : 0);
		res = 17 * res + (m_lastAccessTime != null ? m_lastAccessTime.hashCode() : 0);
		return 17 * res + (m_originalCreationTime != null ? m_originalCreationTime.hashCode() : 0);
	}

	@Override
	public String toString()
	{
		return (m_inLocalHeader ? "l " : "") + "last mod time: " + (m_lastModificationTime != null ? m_lastModificationTime : "<null>") + ", last access time: " + (m_lastAccessTime != null ? m_lastAccessTime : "<null>")
				+ ", original creation time: " + (m_originalCreationTime != null ? m_originalCreationTime : "<null>");
	}

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		int size = 1;
		byte flags = (byte) 0;
		if (m_lastModificationTime != null)
		{
			flags = (byte) 1;
			size = 5;
		}
		if (m_lastAccessTime != null)
		{
			flags += (byte) 2;
			size += 4;
		}
		if (m_originalCreationTime != null)
		{
			flags += (byte) 4;
			size += 4;
		}
		byte[] res = new byte[size];
		res[0] = flags;
		int pos = 1;
		if (m_lastModificationTime != null)
		{
			byte[] lm = UnsignedInteger.valueOf(m_lastModificationTime.getTime() / 1000).getBigEndianByteArray();
			res[pos++] = lm[0];
			res[pos++] = lm[1];
			res[pos++] = lm[2];
			res[pos++] = lm[3];
		}
		if (m_lastAccessTime != null)
		{
			byte[] at = UnsignedInteger.valueOf(m_lastAccessTime.getTime() / 1000).getBigEndianByteArray();
			res[pos++] = at[0];
			res[pos++] = at[1];
			res[pos++] = at[2];
			res[pos++] = at[3];
		}
		if (m_originalCreationTime != null)
		{
			byte[] oc = UnsignedInteger.valueOf(m_originalCreationTime.getTime() / 1000).getBigEndianByteArray();
			res[pos++] = oc[0];
			res[pos++] = oc[1];
			res[pos++] = oc[2];
			res[pos++] = oc[3];
		}
		return res;
	}
}
