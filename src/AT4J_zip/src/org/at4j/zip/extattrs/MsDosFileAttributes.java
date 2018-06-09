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
 * This is an enumeration over the different MS DOS file attributes. It is used
 * by the {@link MsDosExternalFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see MsDosExternalFileAttributes
 */
public enum MsDosFileAttributes
{
	READ_ONLY("read only", 1), HIDDEN_FILE("hidden file", 2), SYSTEM_FILE("system file", 4), VOLUME_LABEL("volume label", 8), SUB_DIRECTORY("sub directory", 16), ARCHIVE("archive", 32);

	/**
	 * This constant contains all MS DOS file attributes.
	 */
	public static final Set<MsDosFileAttributes> ALL = Collections.unmodifiableSet(EnumSet.allOf(MsDosFileAttributes.class));

	private final String m_tag;
	private final int m_encodedValue;

	private MsDosFileAttributes(String tag, int encodedValue)
	{
		m_tag = tag;
		m_encodedValue = encodedValue;
	}

	/**
	 * Get the bit flag value that is used to represent this attribute when
	 * encoded in a Zip entry.
	 */
	public int getEncodedValue()
	{
		return m_encodedValue;
	}

	@Override
	public String toString()
	{
		return m_tag;
	}
}
