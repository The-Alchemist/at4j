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

import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.builder.ZipEntrySettings;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.EntityView;
import org.entityfs.el.AbsoluteLocation;

/**
 * This is the {@link ZipEntryExtraFieldFactory} for extra fields of the type
 * "Unix". It creates objects of the type {@link InfoZipUnixExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see InfoZipUnixExtraField
 */
public class InfoZipUnixExtraFieldFactory implements ZipEntryExtraFieldFactory
{
	private final UnsignedShort m_uid;
	private final UnsignedShort m_gid;

	/**
	 * Create a factory using the default uid and gid (1000/1000).
	 */
	public InfoZipUnixExtraFieldFactory()
	{
		this(1000, 1000);
	}

	/**
	 * Create a factory that will give its created extra fields the supplied uid
	 * and gid.
	 * @param uid The uid to give created extra fields.
	 * @param gid The gid to give created extra fields.
	 */
	public InfoZipUnixExtraFieldFactory(int uid, int gid)
	{
		m_uid = UnsignedShort.valueOf(uid);
		m_gid = UnsignedShort.valueOf(gid);
	}

	public UnsignedShort getCode()
	{
		return InfoZipUnixExtraField.CODE;
	}

	public InfoZipUnixExtraField create(boolean inLocalHeader, AbsoluteLocation loc, UnixEntityType entityType, Object entryToZip, ZipEntrySettings effectiveSettings)
	{
		Date lastModified, lastAccessTime;
		if (entryToZip instanceof EntityView)
		{
			lastModified = new Date(((EntityView) entryToZip).getLastModified());
			// TODO: check for ECNtfsAttributes or ECUnixAttributes capability
		}
		else
		{
			lastModified = new Date();
		}
		lastAccessTime = new Date();
		return inLocalHeader ? new InfoZipUnixExtraField(lastAccessTime, lastModified, m_uid, m_gid) : new InfoZipUnixExtraField(lastAccessTime, lastModified);
	}
}
