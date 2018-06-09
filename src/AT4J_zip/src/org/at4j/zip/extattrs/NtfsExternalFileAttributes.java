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

import java.util.EnumSet;
import java.util.Set;

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.ZipVersionMadeBy;

/**
 * This object represents the external file attributes for NTFS files. It
 * contains a set of {@link NtfsFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class NtfsExternalFileAttributes implements ZipExternalFileAttributes
{
	/**
	 * The default file attributes (archive).
	 */
	public static final NtfsExternalFileAttributes DEFAULT_FILE_ATTRIBUTES = new NtfsExternalFileAttributes(NtfsFileAttributes.ARCHIVE);

	/**
	 * The default directory attributes (archive).
	 */
	public static final NtfsExternalFileAttributes DEFAULT_DIRECTORY_ATTRIBUTES = new NtfsExternalFileAttributes(NtfsFileAttributes.ARCHIVE);

	private final EnumSet<NtfsFileAttributes> m_attributes;

	/**
	 * Create a new NTFS external file attributes object.
	 * @param attributes The attributes for the entity.
	 */
	public NtfsExternalFileAttributes(NtfsFileAttributes... attributes)
	{
		m_attributes = EnumSet.noneOf(NtfsFileAttributes.class);
		for (NtfsFileAttributes attr : attributes)
		{
			m_attributes.add(attr);
		}
	}

	/**
	 * Create a new NTFS external file attributes object.
	 * @param s The attributes for the entity.
	 */
	public NtfsExternalFileAttributes(Set<NtfsFileAttributes> s)
	{
		m_attributes = EnumSet.copyOf(s);
	}

	/**
	 * Get (a copy of) the attributes for the entity.
	 * @return The attributes for the entity.
	 */
	public EnumSet<NtfsFileAttributes> getAttributes()
	{
		// Return a defensive copy
		return m_attributes.clone();
	}

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.WINDOWS_NTFS;
	}

	/**
	 * Is the specified attribute set in this object?
	 * @param attr The attribute to test for.
	 * @return {@code true} if the attribute is set in this object.
	 */
	public boolean isSet(NtfsFileAttributes attr)
	{
		return m_attributes.contains(attr);
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof NtfsExternalFileAttributes))
		{
			return m_attributes.equals(((NtfsExternalFileAttributes) o).m_attributes);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return m_attributes.hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder res = new StringBuilder("NTFS file attributes (");
		for (NtfsFileAttributes attr : m_attributes)
		{
			res.append(attr.toString()).append(", ");
		}
		return res.append(")").toString();
	}

	/**
	 * Get the encoded value for this set of attributes as it is stored in the
	 * Zip file.
	 */
	public UnsignedInteger getEncodedValue()
	{
		int res = 0;
		for (NtfsFileAttributes attr : m_attributes)
		{
			res += attr.getCode();
		}
		return UnsignedInteger.valueOf(res);
	}
}
