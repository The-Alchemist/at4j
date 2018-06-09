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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.zip.ZipFileParseException;
import org.entityfs.el.AbsoluteLocation;

/**
 * This parser is used to parse extra fields of the type
 * {@link UnicodePathExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnicodePathExtraField
 */
public class UnicodePathExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final UnicodePathExtraFieldParser INSTANCE = new UnicodePathExtraFieldParser();

	public UnsignedShort getCode()
	{
		return UnicodePathExtraField.CODE;
	}

	public UnicodePathExtraField parse(byte[] barr, boolean inLocalHeader)
	{
		if (barr.length < 5)
		{
			throw new ZipFileParseException("Invalid data in unicode path extra field: It was only " + barr.length + " bytes long");
		}
		CharBuffer cb = Charsets.UTF8.decode(ByteBuffer.wrap(barr, 5, barr.length - 5));
		char[] carr = new char[cb.limit()];
		int carrLen = carr.length;
		cb.get(carr);
		boolean directory = carr[carrLen - 1] == '\\';
		String s = directory ? new String(carr, 0, carrLen - 1) : new String(carr);
		return new UnicodePathExtraField(inLocalHeader, new AbsoluteLocation("/" + s.replace('\\', '/')), directory);
	}
}
