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
package org.at4j.archive;

import org.entityfs.el.EntityLocation;

/**
 * This interface defines a symbolic link entry in an {@link Archive}.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <T> The type of entries in the archive.
 * @param <U> The type of directory entries in the archive.
 */
public interface ArchiveSymbolicLinkEntry<T extends ArchiveEntry<T, U>, U extends ArchiveDirectoryEntry<T, U>> extends ArchiveEntry<T, U>
{
	/**
	 * Get the link target location. This can either be an
	 * {@link org.entityfs.el.AbsoluteLocation} or a
	 * {@link org.entityfs.el.RelativeLocation}.
	 * @return The link's target location.
	 */
	EntityLocation<?> getLinkTarget();
}
