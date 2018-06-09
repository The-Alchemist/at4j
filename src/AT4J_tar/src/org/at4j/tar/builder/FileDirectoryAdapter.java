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

import java.io.File;

/**
 * This object adapts a {@link java.io.File} directory to the
 * {@link DirectoryAdapter} interface.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class FileDirectoryAdapter implements DirectoryAdapter<File>
{
	private final File m_directory;

	/**
	 * Create a new adapter.
	 * @param f The directory to adapt. This directory must exist (and be a
	 * directory).
	 */
	public FileDirectoryAdapter(File f)
	{
		// Null check
		f.getClass();

		assert f.exists();
		assert f.isDirectory();

		m_directory = f;
	}

	public File getAdapted()
	{
		return m_directory;
	}
}
