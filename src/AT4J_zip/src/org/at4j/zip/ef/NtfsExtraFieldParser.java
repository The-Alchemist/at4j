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

import org.at4j.support.lang.UnsignedLong;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.util.WinNtTime;
import org.at4j.zip.ZipFileParseException;

/**
 * This parser is used for parsing extra fields of the type
 * {@link NtfsExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NtfsExtraField
 */
public class NtfsExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final NtfsExtraFieldParser INSTANCE = new NtfsExtraFieldParser();

	public UnsignedShort getCode()
	{
		return NtfsExtraField.CODE;
	}

	public NtfsExtraField parse(byte[] barr, boolean inLocalHeader) throws ZipFileParseException
	{
		int pos = 4;
		UnsignedShort tag1 = UnsignedShort.fromBigEndianByteArray(barr, pos);
		if (tag1.intValue() != 1)
		{
			throw new ZipFileParseException("Illegal value " + tag1 + " for Tag1 in an NTFS extra field. It must be 1");
		}
		pos += 2;
		UnsignedShort size1 = UnsignedShort.fromBigEndianByteArray(barr, pos);
		if (size1.intValue() != 24)
		{
			throw new ZipFileParseException("Illegal value " + size1 + " for Tag1 size in an NTFS extra field. It must be 24");
		}
		pos += 2;
		WinNtTime lastModTime = new WinNtTime(UnsignedLong.fromBigEndianByteArray(barr, pos));
		pos += 8;
		WinNtTime lastAccessTime = new WinNtTime(UnsignedLong.fromBigEndianByteArray(barr, pos));
		pos += 8;
		WinNtTime creationTime = new WinNtTime(UnsignedLong.fromBigEndianByteArray(barr, pos));
		return new NtfsExtraField(inLocalHeader, lastModTime, lastAccessTime, creationTime);
	}
}
