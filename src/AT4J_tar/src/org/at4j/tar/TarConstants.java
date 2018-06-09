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
package org.at4j.tar;

/**
 * This class contains Tar constants. These are useful for low-level work with
 * Tar files.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class TarConstants
{
	/**
	 * The size of a tar file block is 512 bytes.
	 */
	public static final int BLOCK_SIZE = 512;

	/**
	 * The type flag for a file.
	 * @see #ALT_FILE_TYPE_FLAG
	 */
	public static final char FILE_TYPE_FLAG = '0';

	/**
	 * The alternative type flag for a file.
	 * @see #FILE_TYPE_FLAG
	 */
	public static final char ALT_FILE_TYPE_FLAG = '\0';

	/**
	 * The type flag for a symbolic link.
	 */
	public static final char SYMBOLIC_LINK_TYPE_FLAG = '2';

	/**
	 * The type flag for a directory.
	 */
	public static final char DIRECTORY_TYPE_FLAG = '5';

	// Hidden constructor
	private TarConstants()
	{
		// Nothing
	}
}
