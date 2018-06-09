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

import java.nio.charset.Charset;
import java.util.zip.CRC32;

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.zip.builder.ZipBuilderConfiguration;

/**
 * This extra field contains the comment for a Zip entry encoded in UTF-8.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnicodeCommentExtraFieldFactory
 */
public class UnicodeCommentExtraField implements ZipEntryExtraField
{
	/**
	 * The code that is used to identify this extra field in a Zip file.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0x6375);

	private final boolean m_inLocalHeader;
	// May be null
	private final String m_comment;

	/**
	 * Create a new Unicode comment extra field.
	 * @param inLocalHeader Is the extra field in the Zip entry's local header
	 * or in the Zip file's central directory.
	 * @param comment The comment.
	 */
	public UnicodeCommentExtraField(boolean inLocalHeader, String comment)
	{
		// Comment may be null

		m_inLocalHeader = inLocalHeader;
		m_comment = comment;
	}

	public boolean isInLocalHeader()
	{
		return m_inLocalHeader;
	}

	public String getComment()
	{
		return m_comment != null ? m_comment : "";
	}

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		byte[] sarr;
		if ((m_comment == null) || (m_comment.length() == 0))
		{
			sarr = new byte[0];
		}
		else
		{
			sarr = Charsets.getBytes(m_comment, Charsets.UTF8);
		}
		byte[] res = new byte[5 + sarr.length];
		res[0] = (byte) 1;

		// Calculate checksum for the path. Use the default character encoding.
		byte[] dsarr;
		Charset textEncCharset = builder.getTextEncodingCharset();
		if ((sarr.length == 0) || textEncCharset.equals(Charsets.UTF8))
		{
			dsarr = sarr;
		}
		else
		{
			dsarr = Charsets.getBytes(m_comment, textEncCharset);
		}

		CRC32 checksum = new CRC32();
		checksum.update(dsarr);
		byte[] crc = UnsignedInteger.valueOf(checksum.getValue()).getBigEndianByteArray();
		res[1] = crc[0];
		res[2] = crc[1];
		res[3] = crc[2];
		res[4] = crc[3];

		System.arraycopy(sarr, 0, res, 5, sarr.length);
		return res;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof UnicodeCommentExtraField))
		{
			UnicodeCommentExtraField f2 = (UnicodeCommentExtraField) o;
			return (m_inLocalHeader == f2.m_inLocalHeader) && (m_comment.equals(f2.m_comment));
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return m_comment.hashCode() + (m_inLocalHeader ? 1 : 0);
	}

	@Override
	public String toString()
	{
		return m_comment;
	}
}
