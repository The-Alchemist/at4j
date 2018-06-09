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
package org.at4j.zip.comp;

import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipGeneralPurposeBitFlags;

/**
 * This interface defines a factory object for creating a specific kind of
 * {@link ZipEntryCompressionMethod} object. It is used by the
 * {@link ZipEntryCompressionMethodRegistry}.
 * <p>
 * For compression methods that don't have any configurable properties, this
 * interface is often implemented by the {@link ZipEntryCompressionMethod} class
 * itself.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntryCompressionMethodRegistry
 */
public interface ZipEntryCompressionMethodFactory
{
	/**
	 * Get the unique code identifying this compression method.
	 * @return The unique code identifying this compression method.
	 */
	UnsignedShort getCode();

	/**
	 * Create a compression method instance.
	 * @param gbBitFlags The Zip entry's general purpose bit flags. They may
	 * contain compression algorithm parameters.
	 * @return The Zip entry compression method.
	 */
	ZipEntryCompressionMethod create(ZipGeneralPurposeBitFlags gbBitFlags);
}
