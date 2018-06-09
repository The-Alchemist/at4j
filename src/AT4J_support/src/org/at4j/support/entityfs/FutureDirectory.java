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

import java.util.concurrent.locks.Lock;

import org.entityfs.Directory;
import org.entityfs.ETDirectory;
import org.entityfs.ostrat.OverwriteResult;
import org.entityfs.ostrat.OverwriteStrategy;

/**
 * This is a {@link PotentialDirectory} that does not exist at the time when the
 * object is created. It is created by the first call to the
 * {@link #getDirectory()} method.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class FutureDirectory implements PotentialDirectory
{
	private final PotentialDirectory m_parent;
	private final String m_name;
	private final OverwriteStrategy m_overwriteStrategy;
	private final long m_lastModificationTime;

	// This is set when the directory is created
	private boolean m_hasCreatedDirectory = false;
	private Directory m_directory;

	/**
	 * Create a new future directory object.
	 * @param parent The parent directory.
	 * @param name The name of the directory.
	 * @param lastModificationTime The last modification time of the directory.
	 */
	public FutureDirectory(PotentialDirectory parent, String name, OverwriteStrategy os, long lastModificationTime)
	{
		// Null checks
		parent.getClass();
		name.getClass();
		os.getClass();

		m_parent = parent;
		m_name = name;
		m_overwriteStrategy = os;
		m_lastModificationTime = lastModificationTime;
	}

	public Directory getDirectory()
	{
		if (!m_hasCreatedDirectory)
		{
			// Try to create it
			Directory parent = m_parent.getDirectory();
			if (parent != null)
			{
				Lock wl = parent.lockForWriting();
				try
				{
					OverwriteResult ores = m_overwriteStrategy.overwrite(parent, m_name, ETDirectory.TYPE);
					if (ores == OverwriteResult.CAN_CREATE_NEW_ENTITY)
					{
						m_directory = (Directory) parent.newEntity(ETDirectory.TYPE, m_name, null);
						Lock dwl = m_directory.lockForWriting();
						try
						{
							m_directory.setLastModified(m_lastModificationTime);
						}
						finally
						{
							dwl.unlock();
						}
					}
					else if (ores == OverwriteResult.KEPT_OLD_DIRECTORY)
					{
						parent.getEntityOrNull(m_name).setLastModified(m_lastModificationTime);
					}
				}
				finally
				{
					wl.unlock();
				}
			}
			m_hasCreatedDirectory = true;
		}
		return m_directory;
	}
}
