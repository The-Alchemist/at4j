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
package org.at4j.zip.builder;

import org.at4j.zip.ZipInternalFileAttributes;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.el.AbsoluteLocation;

/**
 * This interface defines a strategy for setting the internal file attributes on
 * a file that is added to a Zip archive. It is used by the {@link ZipBuilder}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipInternalFileAttributes
 */
public interface InternalFileAttributesStrategy
{
	/**
	 * Create internal file attributes based on the supplied information.
	 * @param type The type of entity that is to be added to a Zip archive.
	 * @param loc The location of the entity.
	 * @return Internal attributes for the entity.
	 */
	ZipInternalFileAttributes createInternalFileAttributes(UnixEntityType type, AbsoluteLocation loc);
}
