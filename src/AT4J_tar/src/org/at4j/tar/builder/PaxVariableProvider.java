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
package org.at4j.tar.builder;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.at4j.archive.builder.ArchiveEntryAddException;
import org.entityfs.el.AbsoluteLocation;

/**
 * Implementations of this interface is used by the {@link PaxTarEntryStrategy}
 * to add more Pax variables to Pax headers.
 * @author Karl Gustafsson
 * @since 1.0
 * @see PaxTarEntryStrategy
 */
public interface PaxVariableProvider
{
	/**
	 * Add Pax variables for the entity that is being added to the Tar archive
	 * to the supplied map.
	 * @param variables The map to add variables to.
	 * @param entity The entity that is being added to the Tar archive. This is
	 * a file, a directory or an {@link InputStream}.
	 * @param location The location of the entry in the archive.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the entity to add was last modified.
	 * @throws ArchiveEntryAddException On errors.
	 */
	void addVariables(Map<String, String> variables, Object entity, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified) throws ArchiveEntryAddException;
}
