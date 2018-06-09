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
 * This is a potential directory that already exists.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class ExistingDirectory implements PotentialDirectory
{
	private final Directory m_directory;

	public ExistingDirectory(Directory dir)
	{
		m_directory = dir;
	}

	public Directory getDirectory()
	{
		return m_directory;
	}
}
