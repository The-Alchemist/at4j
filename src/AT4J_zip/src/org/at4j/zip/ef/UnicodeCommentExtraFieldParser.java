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

/**
 * This parser is used to parse extra fields of the type
 * {@link UnicodeCommentExtraField}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnicodeCommentExtraField
 */
public class UnicodeCommentExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final UnicodeCommentExtraFieldParser INSTANCE = new UnicodeCommentExtraFieldParser();

	public UnsignedShort getCode()
	{
		return UnicodeCommentExtraField.CODE;
	}

	public UnicodeCommentExtraField parse(byte[] barr, boolean inLocalHeader)
	{
		if (barr.length < 5)
		{
			throw new ZipFileParseException("Invalid data in unicode comment extra field: It was only " + barr.length + " bytes long");
		}
		else if (barr.length == 5)
		{
			return new UnicodeCommentExtraField(inLocalHeader, null);
		}
		else
		{
			CharBuffer cb = Charsets.UTF8.decode(ByteBuffer.wrap(barr, 5, barr.length - 5));
			char[] carr = new char[cb.limit()];
			cb.get(carr);
			return new UnicodeCommentExtraField(inLocalHeader, new String(carr));
		}
	}
}
