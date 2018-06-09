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

import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipFileParseException;
import org.at4j.zip.ZipVersionMadeBy;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This is a parser for {@link UnixExternalFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnixExternalFileAttributes
 */
public class UnixExternalFileAttributesParser implements ZipExternalFileAttributesParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final UnixExternalFileAttributesParser INSTANCE = new UnixExternalFileAttributesParser();

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.UNIX;
	}

	public UnixExternalFileAttributes parse(ZipVersionMadeBy vmb, byte[] barr)
	{
		if (vmb != ZipVersionMadeBy.UNIX)
		{
			throw new ZipFileParseException("Expected version made by " + ZipVersionMadeBy.UNIX + ". Was " + vmb);
		}
		if (barr.length != 4)
		{
			throw new ZipFileParseException("Invalid Zip file. This parser requires 4 bytes. Got " + barr.length);
		}

		// Only the last two bytes are significant.
		int efa = UnsignedShort.fromBigEndianByteArray(barr, 2).intValue();
		int typeCode = (efa >> 12) & 017;
		UnixEntityType et = typeCode != 0 ? UnixEntityType.forCode(typeCode) : UnixEntityType.DIRECTORY;
		int modeCode = efa & 07777;
		UnixEntityMode em = modeCode != 0 ? UnixEntityMode.forCode(modeCode) : UnixEntityMode.forCode(0775);
		return new UnixExternalFileAttributes(et, em);
	}
}
