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
package org.at4j.support.entityfs;

import org.entityfs.Directory;

/**
 * This is a directory that may or may not exist.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface PotentialDirectory
{
	/**
	 * Get the directory, creating it if necessary.
	 * <p>
	 * If, for some reason, the directory could not be created without that
	 * being an error (due to an {@link org.entityfs.ostrat.OverwriteStrategy},
	 * for instance), this method should return {@code null}.
	 * @return The directory or {@code null} if the directory could not be
	 * created and that is not an error.
	 */
	Directory getDirectory();
}
