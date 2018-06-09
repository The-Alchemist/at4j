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
package org.at4j.support.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.at4j.support.lang.At4JException;

/**
 * This class contains {@link Charset} constants for commonly used charsets and
 * utility methods for converting between byte and character data.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class Charsets
{
	/**
	 * The UTF-8 charset.
	 */
	public static final Charset UTF8 = Charset.forName("utf8");

	/**
	 * The UTF-16 charset.
	 */
	public static final Charset UTF16 = Charset.forName("utf16");

	/**
	 * The US-ASCII charset.
	 */
	public static final Charset ASCII = Charset.forName("ascii");

	/** Hidden constructor. */
	private Charsets()
	{
		// Nothing
	}

	/**
	 * Convert the given text to byte data using the supplied charset.
	 * @param text The text to convert.
	 * @param cs The charset to use for converting the text.
	 * @return Byte data.
	 * @throws At4JException On character coding errors. See
	 * {@link CharacterCodingException}.
	 */
	public static byte[] getBytes(String text, Charset cs) throws At4JException
	{
		CharsetEncoder ce = cs.newEncoder();
		ByteBuffer bb;
		try
		{
			bb = ce.encode(CharBuffer.wrap(text));
		}
		catch (CharacterCodingException e)
		{
			throw new At4JException(e);
		}
		byte[] res = new byte[bb.limit()];
		bb.get(res);
		return res;
	}
}
