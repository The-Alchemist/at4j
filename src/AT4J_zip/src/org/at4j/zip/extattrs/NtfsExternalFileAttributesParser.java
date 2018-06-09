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
package org.at4j.zip.extattrs;

import java.util.EnumSet;

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.ZipFileParseException;
import org.at4j.zip.ZipVersionMadeBy;

/**
 * This parser parses attributes of the type {@link NtfsExternalFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class NtfsExternalFileAttributesParser implements ZipExternalFileAttributesParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final NtfsExternalFileAttributesParser INSTANCE = new NtfsExternalFileAttributesParser();

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.WINDOWS_NTFS;
	}

	public NtfsExternalFileAttributes parse(ZipVersionMadeBy vmb, byte[] barr)
	{
		if (barr.length != 4)
		{
			throw new ZipFileParseException("Invalid Zip file. This parser requires 4 bytes. Got " + barr.length);
		}

		int i = UnsignedInteger.fromBigEndianByteArray(barr).intValue();

		EnumSet<NtfsFileAttributes> attrset = EnumSet.noneOf(NtfsFileAttributes.class);
		for (NtfsFileAttributes attr : NtfsFileAttributes.ALL)
		{
			if ((i & attr.getCode()) > 0)
			{
				attrset.add(attr);
			}
		}
		return new NtfsExternalFileAttributes(attrset);
	}
}
