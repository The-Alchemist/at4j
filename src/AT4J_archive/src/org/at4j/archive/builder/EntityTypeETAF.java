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

import org.entityfs.EntityType;
import org.entityfs.support.filter.AbstractConvenientFilter;

/**
 * This filter matches a specific entity type.
 * <p>
 * {@link FileETAF} or {@link DirectoryETAF} may be more convenient to use.
 * <p>
 * "ETAF" stands for {@code EntryToArchiveFilter}
 * @author Karl Gustafsson
 * @since 1.0
 * @see EntityType
 */
public final class EntityTypeETAF extends AbstractConvenientFilter<EntryToArchive> implements EntityToArchiveFilter
{
	private final EntityType m_entityType;

	/**
	 * Create a new filter.
	 * @param et The entity type to match.
	 */
	public EntityTypeETAF(EntityType et)
	{
		// Null check
		et.getClass();
		m_entityType = et;
	}

	public boolean matches(EntryToArchive object)
	{
		return object.getEntityType() == m_entityType;
	}
}
