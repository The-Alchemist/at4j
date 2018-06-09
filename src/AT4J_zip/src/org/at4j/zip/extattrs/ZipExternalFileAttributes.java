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

import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.ZipVersionMadeBy;

/**
 * This is the interface for objects representing the external file attributes
 * of a Zip file entry. The external file attributes is used for storing file
 * metadata in a format that is specific to the software and operating system
 * platform used to create the Zip file.
 * <p>
 * Implementations of this interface should be immutable.
 * <p>
 * Implementers of this interface are encouraged to also implement good {@code
 * equals} and {@code hashCode} methods.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipExternalFileAttributesFactory
 * @see ZipExternalFileAttributesParser
 * @see ZipExternalFileAttributesParserRegistry
 */
public interface ZipExternalFileAttributes
{
	/**
	 * Get the version of the Zip software used to create these external file
	 * attributes.
	 * @return The version of the Zip software.
	 */
	public ZipVersionMadeBy getVersionMadeBy();

	/**
	 * Get the value of the external file attributes encoded for storing in a
	 * Zip file.
	 * @return The value of the external file attributes encoded for storing in
	 * a Zip file.
	 */
	public UnsignedInteger getEncodedValue();
}
