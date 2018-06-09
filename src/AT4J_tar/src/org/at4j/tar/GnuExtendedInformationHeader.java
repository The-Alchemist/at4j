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

import org.entityfs.el.AbsoluteLocation;

/**
 * This may contain data from several extended information headers (for the same
 * Tar entry). The properties of this object are either set or not set.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class GnuExtendedInformationHeader implements TarEntryHeader
{
	private Boolean m_directory;
	private AbsoluteLocation m_fileName;
	private String m_linkName;

	GnuExtendedInformationHeader()
	{
		// Nothing
	}

	Boolean isDirectory()
	{
		return m_directory;
	}

	void setDirectory(Boolean directory)
	{
		m_directory = directory;
	}

	AbsoluteLocation getFileName()
	{
		return m_fileName;
	}

	void setFileName(AbsoluteLocation fileName)
	{
		m_fileName = fileName;
	}

	String getLinkName()
	{
		return m_linkName;
	}

	void setLinkName(String linkName)
	{
		m_linkName = linkName;
	}
}
