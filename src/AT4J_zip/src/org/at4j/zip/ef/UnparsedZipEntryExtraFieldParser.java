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

import org.at4j.support.lang.UnsignedShort;

/**
 * This object is used to handle extra fields that are not supported. It creates
 * {@link UnparsedZipEntryExtraField} objects.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnparsedZipEntryExtraField
 */
public class UnparsedZipEntryExtraFieldParser implements ZipEntryExtraFieldParser
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final UnparsedZipEntryExtraFieldParser INSTANCE = new UnparsedZipEntryExtraFieldParser();

	/**
	 * This parser does not have a code. It is used for all codes for which
	 * there are no custom parser registered.
	 */
	public UnsignedShort getCode()
	{
		return UnsignedShort.ZERO;
	}

	/**
	 * @return An {@link UnparsedZipEntryExtraField} object.
	 */
	public UnparsedZipEntryExtraField parse(byte[] barr, boolean inLocalHeader)
	{
		return new UnparsedZipEntryExtraField(barr, inLocalHeader);
	}
}
