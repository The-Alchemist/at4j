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

import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipFileParseException;

/**
 * This parser is used to parse extra fields of the type
 * {@link NewInfoZipUnixExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NewInfoZipUnixExtraField
 */
public class NewInfoZipUnixExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final NewInfoZipUnixExtraFieldParser INSTANCE = new NewInfoZipUnixExtraFieldParser();

	public UnsignedShort getCode()
	{
		return NewInfoZipUnixExtraField.CODE;
	}

	public NewInfoZipUnixExtraField parse(byte[] barr, boolean inLocalHeader)
	{
		if (inLocalHeader)
		{
			if (barr.length != 4)
			{
				throw new ZipFileParseException("Could not create a local header extra field from a byte array that is " + barr.length + " bytes long. Expected 4 bytes.");
			}
			UnsignedShort uid = UnsignedShort.fromBigEndianByteArray(barr, 0);
			UnsignedShort gid = UnsignedShort.fromBigEndianByteArray(barr, 2);
			return new NewInfoZipUnixExtraField(uid, gid);
		}
		else
		{
			if (barr.length != 0)
			{
				throw new ZipFileParseException("Could not create a central header extra field from a byte array that is " + barr.length + " bytes long. Expected 0 bytes.");
			}
			return NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION;
		}
	}
}
