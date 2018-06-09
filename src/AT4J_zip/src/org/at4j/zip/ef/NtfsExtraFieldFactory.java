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
import org.at4j.support.util.WinNtTime;
import org.at4j.zip.builder.ZipEntrySettings;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.EntityView;
import org.entityfs.el.AbsoluteLocation;

/**
 * This is a factory for PKWare WIN 95/Win NT extra field objects.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NtfsExtraField
 */
public class NtfsExtraFieldFactory implements ZipEntryExtraFieldFactory
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final NtfsExtraFieldFactory INSTANCE = new NtfsExtraFieldFactory();

	public UnsignedShort getCode()
	{
		return NtfsExtraField.CODE;
	}

	public NtfsExtraField create(boolean inLocalHeader, AbsoluteLocation loc, UnixEntityType entityType, Object entryToZip, ZipEntrySettings effectiveSettings)
	{
		Date now = new Date();
		WinNtTime lastAccessTime = new WinNtTime(now);
		WinNtTime lastModified;
		if (entryToZip instanceof EntityView)
		{
			lastModified = new WinNtTime(new Date(((EntityView) entryToZip).getLastModified()));
		}
		else
		{
			lastModified = lastAccessTime;
		}
		return new NtfsExtraField(inLocalHeader, lastModified, lastAccessTime, lastModified);
	}
}
