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

import org.entityfs.DataSource;
import org.entityfs.Directory;

/**
 * This is used by the {@link TarExtractor}.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class TarExtractorEntryHandler implements TarEntryHandlerDelegate
{
	private final TarExtractSpecification m_spec;
	private final Directory m_targetRoot;

	TarExtractorEntryHandler(TarExtractSpecification spec, Directory targetRoot)
	{
		m_spec = spec;
		m_targetRoot = targetRoot;
	}

	public long handle(TarEntryHeaderData ehd, DataSource src)
	{
		if ((m_spec.getFilter() == null) || (m_spec.getFilter().matches(ehd)))
		{
			m_spec.getEntryExtractionStrategy().extract(ehd, src, m_targetRoot, m_spec);
			return 0;
		}
		else
		{
			// Skip past the entry
			return ehd.getFileSize();
		}
	}
}
