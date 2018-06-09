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

import org.entityfs.support.filter.AbstractConvenientFilter;
import org.entityfs.support.util.regexp.Glob;

/**
 * This filter matches Tar entries with names that match a {@link Glob} pattern.
 * It can be used with the {@link TarExtractor}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarExtractor
 * @see TarExtractSpecification
 */
public final class TarEntryNameGlobFilter extends AbstractConvenientFilter<TarEntryHeaderData> implements TarEntryHeaderDataFilter
{
	private final Glob m_glob;

	/**
	 * Create a filter using the supplied glob pattern.
	 * @param glob The glob pattern.
	 */
	public TarEntryNameGlobFilter(String glob)
	{
		m_glob = Glob.compile(glob);
	}

	/**
	 * Create a filter using the supplied glob pattern.
	 * @param glob The glob pattern.
	 */
	public TarEntryNameGlobFilter(Glob glob)
	{
		// Null check
		glob.getClass();

		m_glob = glob;
	}

	public boolean matches(TarEntryHeaderData object)
	{
		return m_glob.matcher(object.getLocation().getName()).matches();
	}
}
