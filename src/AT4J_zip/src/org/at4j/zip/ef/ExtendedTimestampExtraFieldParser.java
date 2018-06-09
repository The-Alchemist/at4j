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

/**
 * This is the parser for extra fields of the type
 * {@link ExtendedTimestampExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ExtendedTimestampExtraField
 */
public class ExtendedTimestampExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final ExtendedTimestampExtraFieldParser INSTANCE = new ExtendedTimestampExtraFieldParser();

	public UnsignedShort getCode()
	{
		return ExtendedTimestampExtraField.CODE;
	}

	public ExtendedTimestampExtraField parse(byte[] barr, boolean inLocalHeader)
	{
		boolean modificationTimePresent = (barr[0] & 1) != 0;
		boolean accessTimePresent = inLocalHeader && (barr[0] & 2) != 0;
		boolean creationTimePresent = inLocalHeader && (barr[0] & 4) != 0;
		Date modTime = null;
		Date accessTime = null;
		Date creationTime = null;
		int pos = 1;
		if (modificationTimePresent)
		{
			modTime = new Date(UnsignedInteger.fromBigEndianByteArray(barr, pos).longValue() * 1000);
			pos += UnsignedInteger.SIZE;
		}
		if (accessTimePresent)
		{
			accessTime = new Date(UnsignedInteger.fromBigEndianByteArray(barr, pos).longValue() * 1000);
			pos += UnsignedInteger.SIZE;
		}
		if (creationTimePresent)
		{
			creationTime = new Date(UnsignedInteger.fromBigEndianByteArray(barr, pos).longValue() * 1000);
		}
		return new ExtendedTimestampExtraField(inLocalHeader, modTime, accessTime, creationTime);
	}
}
