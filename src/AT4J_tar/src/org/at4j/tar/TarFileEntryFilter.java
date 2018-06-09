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

/**
 * This filter matches Tar file entries. It can be used with the
 * {@link org.at4j.tar.TarExtractor}.
 * <p>
 * Since this filter does not have any internal state, the singleton instance
 * {@link #FILTER} may be used instead of instantiating the class.
 * @author Karl Gustafsson
 * @since 1.0
 * @see org.at4j.tar.TarExtractor
 * @see TarExtractSpecification
 * @see TarDirectoryEntryFilter
 */
public final class TarFileEntryFilter extends AbstractConvenientFilter<TarEntryHeaderData> implements TarEntryHeaderDataFilter
{
	/**
	 * The singleton instance.
	 */
	public static final TarFileEntryFilter FILTER = new TarFileEntryFilter();

	public boolean matches(TarEntryHeaderData object)
	{
		return (!object.isDirectory()) && ((object.getTypeFlag() == TarConstants.FILE_TYPE_FLAG) || (object.getTypeFlag() == TarConstants.ALT_FILE_TYPE_FLAG));
	}
}