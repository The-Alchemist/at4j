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
package org.at4j.zip.extattrs;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * This is an enumeration over the different NTFS file attributes. It is used by
 * {@link NtfsExternalFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NtfsExternalFileAttributes
 */
public enum NtfsFileAttributes
{
	READ_ONLY(1, "read only"), HIDDEN(2, "hidden"), SYSTEM(4, "system"), DIRECTORY(16, "directory"), ARCHIVE(32, "archive"), DEVICE(64, "device"), NORMAL(128, "normal"), TEMPORARY(256, "temporary"), SPARSE_FILE(512, "sparse file"), REPARSE_POINT(
			1024, "reparse point"), COMPRESSED(2048, "compressed"), OFFLINE(4096, "offline"), NOT_CONTENT_INDEXED(8192, "not content indexed"), ENCRYPTED(16384, "encrypted");

	/**
	 * This constant contains all NTFS file attributes.
	 */
	public static final Set<NtfsFileAttributes> ALL = Collections.unmodifiableSet(EnumSet.allOf(NtfsFileAttributes.class));

	private final int m_code;
	private final String m_tag;

	private NtfsFileAttributes(int code, String tag)
	{
		m_code = code;
		m_tag = tag;
	}

	/**
	 * Get the unique code used to identify this attribute.
	 * @return The unique code used to identify this attribute.
	 */
	public int getCode()
	{
		return m_code;
	}

	@Override
	public String toString()
	{
		return m_tag;
	}
}
