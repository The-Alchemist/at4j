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
package org.at4j.zip;

import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedShort;

/**
 * This object represents a {@link ZipEntry}'s internal file attributes. It
 * contains boolean properties about the Zip entry.
 * <p>
 * Despite the "file" in the name, this kind of attributes is used for all Zip
 * entry types.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipInternalFileAttributes
{
	private boolean m_textFile;
	private boolean m_recordsPrecededByLengthControlField;

	/**
	 * Create an internal file attributes object with all property values set to
	 * {@code false}.
	 */
	public ZipInternalFileAttributes()
	{
		// Nothing
	}

	/**
	 * Create an internal file attributes object with the property values parsed
	 * from the supplied short object read from a Zip file.
	 * @param s The short object, read from a Zip file.
	 */
	public ZipInternalFileAttributes(UnsignedShort s)
	{
		UnsignedByte b1 = UnsignedByte.valueOf(s.intValue());
		m_textFile = b1.isBitSet(0);
		m_recordsPrecededByLengthControlField = b1.isBitSet(1);
	}

	/**
	 * <pre>
	 * The lowest bit of this field indicates, if set, that
	 * the file is apparently an ASCII or text file.  If not
	 * set, that the file apparently contains binary data.
	 * The remaining bits are unused in version 1.0.
	 * </pre>
	 * @return {@code true} if the file is apparently an ASCII or text file.
	 */
	public boolean isTextFile()
	{
		return m_textFile;
	}

	public void setTextFile(boolean b)
	{
		m_textFile = b;
	}

	/**
	 * <pre>
	 * The 0x0002 bit of this field indicates, if set, that a 
	 * 4 byte variable record length control field precedes each 
	 * logical record indicating the length of the record. The 
	 * record length control field is stored in little-endian byte
	 * order.  This flag is independent of text control characters, 
	 * and if used in conjunction with text data, includes any 
	 * control characters in the total length of the record. This 
	 * value is provided for mainframe data transfer support.
	 * </pre>
	 * @return {@code true} if each logical record is preceded by a 4 byte
	 * variable record length control field.
	 */
	public boolean isRecordsPrecededByLengthControlField()
	{
		return m_recordsPrecededByLengthControlField;
	}

	public void setRecordsPrecededByLengthControlField(boolean b)
	{
		m_recordsPrecededByLengthControlField = b;
	}

	/**
	 * Get the internal file attributes encoded for storing in a Zip file.
	 * @return The numerical value for this set of internal file attributes.
	 */
	public UnsignedShort getEncodedValue()
	{
		int res = 0;
		res += m_textFile ? 1 : 0;
		res += m_recordsPrecededByLengthControlField ? 2 : 0;
		return UnsignedShort.valueOf(res);
	}
}
