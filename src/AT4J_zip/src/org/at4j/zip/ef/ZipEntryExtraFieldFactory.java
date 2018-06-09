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
import org.at4j.zip.builder.ZipEntrySettings;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.el.AbsoluteLocation;

/**
 * This factory is used to create Zip entry extra field objects when building
 * Zip files.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntryExtraField
 */
public interface ZipEntryExtraFieldFactory
{
	/**
	 * Get the code that identifies the kind of {@link ZipEntryExtraField}
	 * objects created by this factory in a Zip file.
	 * @return The code.
	 */
	UnsignedShort getCode();

	/**
	 * Create a new {@link ZipEntryExtraField} object based on the supplied
	 * information about the entity to Zip.
	 * @param inLocalHeader Should a local header or a central directory version
	 * of the extra field be created?
	 * @param loc The absolute location of the entry in the Zip file.
	 * @param entityType The type of entity to zip.
	 * @param entityToZip The file system entity to Zip. This may be a
	 * {@link org.entityfs.ReadableFile}, a {@link org.entityfs.DirectoryView},
	 * a {@link java.io.File} directory or an {@link java.io.InputStream}. In
	 * the latter case, this method cannot read any data from the stream.
	 * @param effectiveSettings The effective settings for the entry.
	 * @return A Zip entry extra field.
	 */
	ZipEntryExtraField create(boolean inLocalHeader, AbsoluteLocation loc, UnixEntityType entityType, Object entityToZip, ZipEntrySettings effectiveSettings);
}
