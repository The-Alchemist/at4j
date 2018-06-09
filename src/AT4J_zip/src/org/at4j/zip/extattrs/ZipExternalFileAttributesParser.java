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

import org.at4j.zip.ZipFileParseException;
import org.at4j.zip.ZipVersionMadeBy;

/**
 * This interface defines a parser that is used for parsing external file
 * attributes for a Zip entry from a Zip file.
 * <p>
 * Parsers for different external file attributes formats are stored in the
 * {@link ZipExternalFileAttributesParserRegistry}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipExternalFileAttributes
 * @see ZipExternalFileAttributesParserRegistry
 */
public interface ZipExternalFileAttributesParser
{
	/**
	 * Get the Zip version that is used to make the kind of external file
	 * attributes that are parsed by this parser.
	 * @return The version.
	 */
	ZipVersionMadeBy getVersionMadeBy();

	/**
	 * Parse the external file attributes.
	 * @param vmb The version of the Zip software used to create the Zip entry.
	 * @param barr The unparsed value of the external file attributes record in
	 * the Zip entry.
	 * @return The entry's external file attributes.
	 * @throws ZipFileParseException On parse errors.
	 */
	ZipExternalFileAttributes parse(ZipVersionMadeBy vmb, byte[] barr) throws ZipFileParseException;
}
