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

import org.at4j.zip.builder.ZipBuilderConfiguration;

/**
 * This interface defines a Zip entry extra field. A Zip entry may have zero or
 * more extra fields that store metadata that is not supported in the regular
 * headers or the in the external file attributes.
 * <p>
 * An extra field for an entry is always stored in two variants &ndash; one in
 * the local file header and one in the entry's central directory record. The
 * local header version often contain more data than the central directory
 * version.
 * <p>
 * Each extra field type is identified with a unique code.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface ZipEntryExtraField
{
	/**
	 * Is this field stored in the Zip entry's local header or in the Zip file's
	 * central directory record?
	 * <p>
	 * The same {@code ZipEntryExtraField} type may have different properties
	 * depending on if it is specified in the local or in the central header.
	 * @return {@code true} if this extra field comes from the Zip entry's local
	 * header. {@code false} if it comes from the Zip file's central directory.
	 */
	boolean isInLocalHeader();

	/**
	 * Encode this extra field's data for storing in a Zip file. This is used
	 * when building Zip files.
	 * @param c The configuration for the Zip builder object that is building
	 * the Zip file.
	 * @return A byte array containing the extra field data as it should be
	 * stored in the Zip file. The returned array should <i>not</i> contain the
	 * initial four bytes with the extra field code and the extra field length.
	 */
	byte[] encode(ZipBuilderConfiguration c);
}
