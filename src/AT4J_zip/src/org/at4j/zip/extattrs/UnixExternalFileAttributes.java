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

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.ZipVersionMadeBy;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This object represents file attributes in a Unix system. It has a field for
 * the type of entity and a field for its access mode.
 * <p>
 * Instances of this class are immutable.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnixExternalFileAttributes implements ZipExternalFileAttributes
{
	/**
	 * Default file attributes (file, 0644)
	 */
	public static final UnixExternalFileAttributes DEFAULT_FILE_ATTRIBUTES = new UnixExternalFileAttributes(UnixEntityType.REGULAR_FILE, UnixEntityMode.forCode(0644));

	/**
	 * Default directory attributes (directory, 0755)
	 */
	public static final UnixExternalFileAttributes DEFAULT_DIRECTORY_ATTRIBUTES = new UnixExternalFileAttributes(UnixEntityType.DIRECTORY, UnixEntityMode.forCode(0755));

	private final UnixEntityType m_entityType;
	private final UnixEntityMode m_entityMode;

	/**
	 * Create a {@code UnixFileAttributes} object.
	 * @param uet The entity type.
	 * @param uem The entity's access mode.
	 */
	public UnixExternalFileAttributes(UnixEntityType uet, UnixEntityMode uem)
	{
		// Null checks
		uet.getClass();
		uem.getClass();

		m_entityType = uet;
		m_entityMode = uem;
	}

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.UNIX;
	}

	/**
	 * Get the entity type.
	 * @return The entity type.
	 */
	public UnixEntityType getEntityType()
	{
		return m_entityType;
	}

	/**
	 * Get the entity mode.
	 * @return The entity mode.
	 */
	public UnixEntityMode getEntityMode()
	{
		return m_entityMode;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof UnixExternalFileAttributes))
		{
			UnixExternalFileAttributes fa2 = (UnixExternalFileAttributes) o;
			return (m_entityType == fa2.m_entityType) && (m_entityMode.equals(fa2.m_entityMode));
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return m_entityType.hashCode() ^ m_entityMode.hashCode();
	}

	@Override
	public String toString()
	{
		return m_entityType.toString() + m_entityMode.toString();
	}

	public UnsignedInteger getEncodedValue()
	{
		return UnsignedInteger.valueOf((((long) m_entityMode.getCode()) << 16) + (((long) m_entityType.getCode()) << 28));
	}
}
