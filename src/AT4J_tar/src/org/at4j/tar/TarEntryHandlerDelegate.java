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

/**
 * An implementation of the Tar entry handler delegate is used by the
 * {@link TarFileParser} to deal with each entry that it parses.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface TarEntryHandlerDelegate
{
	/**
	 * Handle a Tar entry.
	 * @param ehd The entry's header data.
	 * @param src The data source. When this method is called it is positioned
	 * at the start of the entry data (at a Tar block boundary). When the method
	 * returns it should be at the same position or at another Tar block
	 * boundary at a higher position in the data source.
	 * @return The minimum number of bytes that should be skipped over to reach
	 * the next Tar entry header.
	 */
	long handle(TarEntryHeaderData ehd, DataSource src);
}
