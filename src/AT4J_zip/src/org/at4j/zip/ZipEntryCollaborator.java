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
package org.at4j.zip;

import java.util.Map;

import org.at4j.archive.ArchiveEntryCollaborator;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.el.AbsoluteLocation;

/**
 * This collaborator object is used by a {@link ZipEntry}:s to access the file
 * object that its parent {@link ZipFile} objects is built on.
 * <p>
 * This is part of the {@link ZipFile} implementation. Clients should never have
 * to bother with this object.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipEntryCollaborator extends ArchiveEntryCollaborator<ZipEntry, ZipDirectoryEntry>
{
	/**
	 * Create a new collaborator.
	 * @param zipFile The Zip file. This file must be locked for reading while
	 * this object is alive.
	 * @param entryMap A map containing all of the entries in the Zip file. This
	 * object will use the {@code Map} instance that is supplied in the argument
	 * (i.e. not make a defensive copy of it). The map does not have to contain
	 * all Zip entries when this object is created, but it must do so before any
	 * client starts to use the Zip file.
	 */
	public ZipEntryCollaborator(RandomlyAccessibleFile zipFile, Map<AbsoluteLocation, ZipEntry> entryMap)
	{
		super(zipFile, entryMap);
	}
}
