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
package org.at4j.util.archive;

import org.at4j.archive.ArchiveDirectoryEntry;
import org.at4j.archive.ArchiveFileEntry;
import org.at4j.archive.ArchiveSymbolicLinkEntry;
import org.at4j.support.entityfs.PotentialDirectory;
import org.entityfs.Directory;

/**
 * This interface is implemented by different strategy objects that help the
 * {@link org.at4j.util.archive.ArchiveExtractor} to extract entries.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface EntryExtractionStrategy
{
	/**
	 * Extract a directory entry to the target directory.
	 * @param dir The directory entry.
	 * @param target The target directory.
	 * @param spec The extraction specification.
	 * @return The new directory or a previously existing directory if there
	 * already was a directory at the target location. If this operation did not
	 * result in there being a directory at the target location (if the target
	 * was occupied by a file that the overwrite strategy did not allow us to
	 * overwrite, for instance), this method returns {@code null}.
	 * @see #postProcessDirectory(ArchiveDirectoryEntry, Directory, ExtractSpecification)
	 */
	Directory extractDirectory(ArchiveDirectoryEntry<?, ?> dir, PotentialDirectory target, ExtractSpecification spec);

	/**
	 * Post-process an extracted directory after all of its child entries have
	 * been extracted. This can be used to set the directory's last modification
	 * time.
	 * @param dir The directory entry.
	 * @param target The extracted directory.
	 * @param spec The extraction specification.
	 * @see #extractDirectory(ArchiveDirectoryEntry, PotentialDirectory, ExtractSpecification)
	 * @since 1.1.1
	 */
	void postProcessDirectory(ArchiveDirectoryEntry<?, ?> dir, Directory target, ExtractSpecification spec);
	
	/**
	 * Extract a file entry to the target directory.
	 * @param f The file entry.
	 * @param target The target directory.
	 * @param spec The extraction specification.
	 */
	void extractFile(ArchiveFileEntry<?, ?> f, PotentialDirectory target, ExtractSpecification spec);

	/**
	 * Extract a symbolic link entry to the target directory.
	 * @param l The symbolic link entry.
	 * @param target The target directory.
	 * @param spec The extraction specification.
	 */
	void extractSymbolicLink(ArchiveSymbolicLinkEntry<?, ?> l, PotentialDirectory target, ExtractSpecification spec);
}
