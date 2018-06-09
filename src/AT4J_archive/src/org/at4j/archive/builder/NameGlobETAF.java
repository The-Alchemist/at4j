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
package org.at4j.archive.builder;

import org.entityfs.support.filter.AbstractConvenientFilter;
import org.entityfs.support.util.regexp.Glob;

/**
 * This filter matches entities with names that match a {@link Glob} pattern
 * (such as {@code .exe}).
 * <p>
 * "ETAF" stands for {@code EntryToArchiveFilter}
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class NameGlobETAF extends AbstractConvenientFilter<EntryToArchive> implements EntityToArchiveFilter
{
	private final Glob m_pattern;

	/**
	 * Create a new filter.
	 * @param pattern The glob pattern.
	 */
	public NameGlobETAF(String pattern)
	{
		// Null check
		pattern.getClass();

		m_pattern = Glob.compile(pattern);
	}

	/**
	 * Create a new filter.
	 * @param pattern The glob pattern.
	 */
	public NameGlobETAF(Glob pattern)
	{
		// Null check
		pattern.getClass();

		m_pattern = pattern;
	}

	public boolean matches(EntryToArchive object)
	{
		return m_pattern.matcher(object.getLocation().getName()).matches();
	}
}
