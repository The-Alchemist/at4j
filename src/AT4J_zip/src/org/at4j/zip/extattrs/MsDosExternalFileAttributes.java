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
 * This object represents the MS DOS file attributes for a Zip entry. It
 * contains a set of {@link MsDosFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see MsDosFileAttributes
 */
public class MsDosExternalFileAttributes implements ZipExternalFileAttributes
{
	public static final MsDosExternalFileAttributes DEFAULT_DIRECTORY_ATTRIBUTES = new MsDosExternalFileAttributes(MsDosFileAttributes.SUB_DIRECTORY);
	public static final MsDosExternalFileAttributes DEFAULT_FILE_ATTRIBUTES = new MsDosExternalFileAttributes(MsDosFileAttributes.ARCHIVE);

	private final EnumSet<MsDosFileAttributes> m_attributes;

	/**
	 * Create a new attributes object.
	 * @param attributes The attributes.
	 */
	public MsDosExternalFileAttributes(MsDosFileAttributes... attributes)
	{
		m_attributes = EnumSet.noneOf(MsDosFileAttributes.class);
		for (MsDosFileAttributes attr : attributes)
		{
			m_attributes.add(attr);
		}
	}

	/**
	 * Create a new attributes object.
	 * @param attributes The attributes.
	 */
	public MsDosExternalFileAttributes(Set<MsDosFileAttributes> attributes)
	{
		m_attributes = EnumSet.copyOf(attributes);
	}

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.MSDOS;
	}

	/**
	 * Get (a copy of) this object's attributes.
	 * @return This object's attributes.
	 */
	public Set<MsDosFileAttributes> getAttributes()
	{
		// Return a defensive copy
		return m_attributes.clone();
	}

	/**
	 * Is the attribute set?
	 * @return {@code true} if the specified attribute is set.
	 */
	public boolean isSet(MsDosFileAttributes attr)
	{
		return m_attributes.contains(attr);
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof MsDosExternalFileAttributes))
		{
			return m_attributes.equals(((MsDosExternalFileAttributes) o).m_attributes);
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
		return "MS DOS external file attributes " + m_attributes;
	}

	public UnsignedInteger getEncodedValue()
	{
		int res = 0;
		for (MsDosFileAttributes attr : m_attributes)
		{
			res += attr.getEncodedValue();
		}
		return UnsignedInteger.valueOf(res);
	}
}
