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

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipFileParseException;

/**
 * This parser is used for parsing extra fields of the type
 * {@link InfoZipUnixExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see InfoZipUnixExtraField
 */
public class InfoZipUnixExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final InfoZipUnixExtraFieldParser INSTANCE = new InfoZipUnixExtraFieldParser();

	public UnsignedShort getCode()
	{
		return InfoZipUnixExtraField.CODE;
	}

	public InfoZipUnixExtraField parse(byte[] barr, boolean inLocalHeader)
	{
		if (inLocalHeader)
		{
			if (barr.length != 12)
			{
				throw new ZipFileParseException("Could not create a local header extra field from a byte array that is " + barr.length + " bytes long. Expected 12 bytes.");
			}
			UnsignedInteger aTime = UnsignedInteger.fromBigEndianByteArray(barr, 0);
			UnsignedInteger mTime = UnsignedInteger.fromBigEndianByteArray(barr, 4);
			UnsignedShort uid = UnsignedShort.fromBigEndianByteArray(barr, 8);
			UnsignedShort gid = UnsignedShort.fromBigEndianByteArray(barr, 10);
			return new InfoZipUnixExtraField(new Date(aTime.longValue() * 1000), new Date(mTime.longValue() * 1000), uid, gid);
		}
		else
		{
			if (barr.length != 8)
			{
				throw new ZipFileParseException("Could not create a central header extra field from a byte array that is " + barr.length + " bytes long. Expected 8 bytes.");
			}
			UnsignedInteger aTime = UnsignedInteger.fromBigEndianByteArray(barr, 0);
			UnsignedInteger mTime = UnsignedInteger.fromBigEndianByteArray(barr, 4);
			return new InfoZipUnixExtraField(new Date(aTime.longValue() * 1000), new Date(mTime.longValue() * 1000));
		}
	}
}
