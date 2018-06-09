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

import org.at4j.zip.builder.ZipBuilderConfiguration;

/**
 * This extra field object is used when the Zip file parser encounters an extra
 * field type that is not supported. This object contains the unparsed extra
 * field data.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UnparsedZipEntryExtraField implements ZipEntryExtraField
{
	private final byte[] m_data;
	private final boolean m_inLocalHeader;

	/**
	 * Create a new unparsed extra field object.
	 * @param data The unparsed data.
	 * @param inLocalHeader Is this field in the Zip entry's local header or in
	 * the Zip file's central directory.
	 */
	public UnparsedZipEntryExtraField(byte[] data, boolean inLocalHeader)
	{
		// Null check
		data.getClass();

		m_data = data;
		m_inLocalHeader = inLocalHeader;
	}

	/**
	 * Get the unparsed data for this extra field.
	 * @return A copy of this extra field's data.
	 */
	public byte[] getData()
	{
		// Defensive copy
		byte[] res = new byte[m_data.length];
		System.arraycopy(m_data, 0, res, 0, m_data.length);
		return res;
	}

	public boolean isInLocalHeader()
	{
		return m_inLocalHeader;
	}

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		return m_data;
	}

	@Override
	public String toString()
	{
		return "unparsed extra field, " + m_data.length + " bytes long";
	}
}
