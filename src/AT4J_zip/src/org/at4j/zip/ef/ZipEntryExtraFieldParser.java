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
import org.at4j.zip.ZipFileParseException;

/**
 * A Zip entry extra field parser is used to parse extra fields when creating a
 * {@link org.at4j.zip.ZipFile} object.
 * <p>
 * Every extra field type has a unique code identifying it. This code can be
 * used to look up a parser implementation using the
 * {@link ZipEntryExtraFieldParserRegistry}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntryExtraField
 * @see ZipEntryExtraFieldParserRegistry
 */
public interface ZipEntryExtraFieldParser
{
	/**
	 * Get the code that identifies the kind of {@link ZipEntryExtraField}
	 * objects created by this factory.
	 * @return The code.
	 */
	UnsignedShort getCode();

	/**
	 * Parse the extra field data and create a {@link ZipEntryExtraField}
	 * object.
	 * @param barr A byte array containing the extra field data. This array does
	 * <i>not</i> contain the initial four bytes identifying the extra field
	 * type and the extra field size.
	 * @param inLocalHeader Is the extra field from the Zip entry's local header
	 * ({@code true}) of from the central header ({@code false})?
	 * @return A Zip entry extra field object.
	 * @throws ZipFileParseException If the contents of the array cannot be
	 * parsed.
	 */
	ZipEntryExtraField parse(byte[] barr, boolean inLocalHeader) throws ZipFileParseException;
}
