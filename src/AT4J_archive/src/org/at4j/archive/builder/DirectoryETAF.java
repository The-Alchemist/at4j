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

import org.entityfs.ETDirectory;
import org.entityfs.support.filter.AbstractConvenientFilter;
import org.entityfs.support.filter.ConvenientFilter;
import org.entityfs.support.filter.Filter;

/**
 * This filter returns true of the entity is a directory.
 * <p>
 * "ETAF" stands for {@code EntryToArchiveFilter}
 * @author Karl Gustafsson
 * @since 1.0
 * @see FileETAF
 */
public final class DirectoryETAF extends AbstractConvenientFilter<EntryToArchive> implements EntityToArchiveFilter
{
	/**
	 * Since this object does not contain any internal state, this singleton
	 * instance may be used instead of creating new objects.
	 */
	public static final DirectoryETAF FILTER = new DirectoryETAF();

	@Override
	public ConvenientFilter<EntryToArchive> and(Filter<? super EntryToArchive> f)
	{
		if (f instanceof FileETAF)
		{
			return FalseETAF.FILTER;
		}
		else
		{
			return super.and(f);
		}
	}

	public boolean matches(EntryToArchive object)
	{
		return object.getEntityType() == ETDirectory.TYPE;
	}
}
