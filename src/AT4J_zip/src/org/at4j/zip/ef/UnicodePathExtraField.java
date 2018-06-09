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
import org.entityfs.el.AbsoluteLocation;

/**
 * This extra field contains the entity's absolute path encoded in UTF-8.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnicodePathExtraFieldFactory
 */
public class UnicodePathExtraField implements ZipEntryExtraField
{
	/**
	 * The code that is used to identify this extra field in a Zip file.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0x7075);

	private final boolean m_inLocalHeader;
	private final AbsoluteLocation m_absolutePath;
	private final boolean m_directory;

	/**
	 * Create a new Unicode extra field.
	 * @param inLocalHeader Is the extra field in the Zip entry's local header
	 * or in the Zip file's central directory.
	 * @param absPath The absolute path of the entry in the Zip archive.
	 * @param directory Is the entry a directory?
	 */
	public UnicodePathExtraField(boolean inLocalHeader, AbsoluteLocation absPath, boolean directory)
	{
		// Null check
		absPath.getClass();

		m_inLocalHeader = inLocalHeader;
		// AbsoluteLocation objects are immutable
		m_absolutePath = absPath;
		m_directory = directory;
	}

	public boolean isInLocalHeader()
	{
		return m_inLocalHeader;
	}

	public AbsoluteLocation getAbsolutePath()
	{
		return m_absolutePath;
	}

	public boolean isDirectory()
	{
		return m_directory;
	}

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		String s = m_absolutePath.getLocation().substring(1).replace('/', '\\');
		if (m_directory)
		{
			s += '\\';
		}
		byte[] sarr = Charsets.getBytes(s, Charsets.UTF8);
		byte[] res = new byte[5 + sarr.length];
		res[0] = (byte) 1;

		// Calculate checksum for the path. Use the default character encoding.
		byte[] dsarr;
		Charset textEncCharset = builder.getFileNameEncodingCharset();
		if (textEncCharset.equals(Charsets.UTF8))
		{
			dsarr = sarr;
		}
		else
		{
			dsarr = Charsets.getBytes(s, textEncCharset);
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
		if ((o != null) && (o instanceof UnicodePathExtraField))
		{
			UnicodePathExtraField f2 = (UnicodePathExtraField) o;
			return (m_inLocalHeader == f2.m_inLocalHeader) && (m_directory == f2.m_directory) && (m_absolutePath.equals(f2.m_absolutePath));
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return m_absolutePath.hashCode() + (m_inLocalHeader ? 1 : 0) + (m_directory ? 1 : 0);
	}

	@Override
	public String toString()
	{
		return m_absolutePath.toString();
	}
}
