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

import java.util.Arrays;

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.ZipVersionMadeBy;
import org.at4j.zip.ef.ZipEntryExtraFieldParserRegistry;

/**
 * This is a fallback {@link ZipExternalFileAttributes} object that is used for
 * versions of the Zip software for which there are no version-specific
 * {@link ZipExternalFileAttributesFactory} objects registered in the
 * {@link ZipEntryExtraFieldParserRegistry}. This object contains the unparsed
 * external file attributes value from the Zip entry header.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnparsedExternalFileAttributes implements ZipExternalFileAttributes
{
	private final ZipVersionMadeBy m_versionMadeBy;
	private final byte[] m_externalFileAttributes;

	public UnparsedExternalFileAttributes(ZipVersionMadeBy vmb, byte[] barr)
	{
		// Null checks
		vmb.getClass();
		barr.getClass();

		if (barr.length != 4)
		{
			throw new IllegalArgumentException("The byte array must be four bytes long. Was " + barr.length);
		}

		m_versionMadeBy = vmb;
		m_externalFileAttributes = barr;
	}

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return m_versionMadeBy;
	}

	/**
	 * Get the unparsed external file attributes for the Zip entry.
	 * @return The unparsed external file attributes for the Zip entry. The
	 * returned array is a copy of the array used internally in this object.
	 */
	public byte[] getUnparsedExternalFileAttributes()
	{
		// Return a defensive copy
		byte[] res = new byte[4];
		System.arraycopy(m_externalFileAttributes, 0, res, 0, 4);
		return res;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof UnparsedExternalFileAttributes))
		{
			return (m_versionMadeBy == ((UnparsedExternalFileAttributes) o).m_versionMadeBy) && Arrays.equals(m_externalFileAttributes, ((UnparsedExternalFileAttributes) o).m_externalFileAttributes);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return 30 * UnsignedInteger.fromBigEndianByteArray(m_externalFileAttributes).intValue() + m_versionMadeBy.ordinal();
	}

	@Override
	public String toString()
	{
		return m_versionMadeBy.toString() + ": " + UnsignedInteger.fromBigEndianByteArray(m_externalFileAttributes).toString();
	}

	public UnsignedInteger getEncodedValue()
	{
		return UnsignedInteger.fromBigEndianByteArray(m_externalFileAttributes);
	}
}
