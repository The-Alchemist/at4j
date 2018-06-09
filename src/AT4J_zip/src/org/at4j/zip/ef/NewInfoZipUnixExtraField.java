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

import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.builder.ZipBuilderConfiguration;

/**
 * This, the "Unix2" Zip entry extra field, contains information on the owner
 * UID/GID of the Zip entry.
 * <p>
 * The local header version contains UID/GID information. The central header
 * version is empty.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NewInfoZipUnixExtraFieldFactory
 * @see InfoZipUnixExtraField
 */
public class NewInfoZipUnixExtraField implements ZipEntryExtraField
{
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0x7855);

	/**
	 * This is the central header version of this extra field. It does not
	 * contain any information.
	 */
	public static final NewInfoZipUnixExtraField CENTRAL_HEADER_VERSION = new NewInfoZipUnixExtraField();

	private final boolean m_inLocalHeader;
	// This is null if the field is from the central header.
	private final UnsignedShort m_uid;
	// This is null if the field is from the central header.
	private final UnsignedShort m_gid;

	/**
	 * This creates the empty, central header version of this extra field.
	 */
	private NewInfoZipUnixExtraField()
	{
		m_inLocalHeader = false;
		m_uid = null;
		m_gid = null;
	}

	/**
	 * This creates the local header version of this extra field.
	 * @param uid The owner user id.
	 * @param gid The owner group id.
	 */
	public NewInfoZipUnixExtraField(UnsignedShort uid, UnsignedShort gid)
	{
		// Null checks
		uid.getClass();
		gid.getClass();

		m_inLocalHeader = true;
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

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		if (m_inLocalHeader)
		{
			byte[] res = new byte[4];
			byte[] uid = m_uid.getBigEndianByteArray();
			res[0] = uid[0];
			res[1] = uid[1];

			byte[] gid = m_gid.getBigEndianByteArray();
			res[2] = gid[0];
			res[3] = gid[1];

			return res;
		}
		else
		{
			return new byte[0];
		}
	}

	@Override
	public String toString()
	{
		return "Unix2 - uid: " + (m_uid != null ? m_uid : "<null>") + ", gid: " + (m_gid != null ? m_gid : "<null>");
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof NewInfoZipUnixExtraField))
		{
			NewInfoZipUnixExtraField o2 = (NewInfoZipUnixExtraField) o;
			if ((!m_inLocalHeader) && (!o2.m_inLocalHeader))
			{
				return true;
			}
			else
			{
				if (m_inLocalHeader && o2.m_inLocalHeader)
				{
					return m_uid.equals(o2.m_uid) && m_gid.equals(o2.m_gid);
				}
			}

		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return m_uid != null ? m_uid.intValue() << 16 + m_gid.intValue() : 0;
	}
}
