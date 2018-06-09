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

import org.at4j.zip.ZipFileParseException;
import org.at4j.zip.ZipVersionMadeBy;

/**
 * This parser is used to parse external file attributes of the type
 * {@link MsDosExternalFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see MsDosExternalFileAttributes
 */
public class MsDosExternalFileAttributesParser implements ZipExternalFileAttributesParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final MsDosExternalFileAttributesParser INSTANCE = new MsDosExternalFileAttributesParser();

	public ZipVersionMadeBy getVersionMadeBy()
	{
		return ZipVersionMadeBy.MSDOS;
	}

	// Documentation on the attributes from
	// http://support.microsoft.com/kb/125019
	//  Bit   Attribute
	//  ---   --------------
	//   7    reserved
	//   6    reserved
	//   5    archive
	//   4    sub-directory
	//   3    volume label
	//   2    system file
	//   1    hidden file
	//   0    read-only file

	public MsDosExternalFileAttributes parse(ZipVersionMadeBy vmb, byte[] barr)
	{
		if (vmb != ZipVersionMadeBy.MSDOS)
		{
			throw new ZipFileParseException("Expected version made by " + ZipVersionMadeBy.MSDOS + ". Was " + vmb);
		}
		if (barr.length != 4)
		{
			throw new ZipFileParseException("Invalid Zip file. This attributes parser requires 4 bytes. Got " + barr.length);
		}

		int b = barr[0];
		EnumSet<MsDosFileAttributes> attrs = EnumSet.noneOf(MsDosFileAttributes.class);
		for (MsDosFileAttributes attr : MsDosFileAttributes.ALL)
		{
			if ((b & attr.getEncodedValue()) > 0)
			{
				attrs.add(attr);
			}
		}
		return new MsDosExternalFileAttributes(attrs);
	}
}
