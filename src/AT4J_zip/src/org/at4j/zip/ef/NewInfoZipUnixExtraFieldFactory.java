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
import org.at4j.zip.builder.ZipEntrySettings;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.el.AbsoluteLocation;

/**
 * This is the {@link ZipEntryExtraFieldFactory} for extra fields of the type
 * "Unix2". It creates objects of the type {@link NewInfoZipUnixExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NewInfoZipUnixExtraField
 */
public class NewInfoZipUnixExtraFieldFactory implements ZipEntryExtraFieldFactory
{
	private final UnsignedShort m_uid;
	private final UnsignedShort m_gid;

	/**
	 * Create a new factory creating extra fields with the default uid and gid
	 * (1000/1000).
	 */
	public NewInfoZipUnixExtraFieldFactory()
	{
		this(1000, 1000);
	}

	/**
	 * Create a new factory creating extra fields with the supplied uid and gid.
	 * @param uid The uid.
	 * @param gid The gid.
	 */
	public NewInfoZipUnixExtraFieldFactory(int uid, int gid)
	{
		m_uid = UnsignedShort.valueOf(uid);
		m_gid = UnsignedShort.valueOf(gid);
	}

	public UnsignedShort getCode()
	{
		return NewInfoZipUnixExtraField.CODE;
	}

	public NewInfoZipUnixExtraField create(boolean inLocalHeader, AbsoluteLocation loc, UnixEntityType entityType, Object entryToZip, ZipEntrySettings effectiveSettings)
	{
		if (inLocalHeader)
		{
			return new NewInfoZipUnixExtraField(m_uid, m_gid);
		}
		else
		{
			return NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION;
		}
	}
}
