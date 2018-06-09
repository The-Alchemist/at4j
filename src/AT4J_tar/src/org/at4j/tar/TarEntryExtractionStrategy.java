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
import org.entityfs.support.exception.WrappedIOException;

/**
 * This strategy object is used by the {@link TarExtractor} to extract the tar
 * entries.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarExtractSpecification
 */
public interface TarEntryExtractionStrategy
{
	/**
	 * Extract the current Tar entry.
	 * @param headerData The Tar entry's header data.
	 * @param src The data source. When this method is called, the data source
	 * is positioned at the start of the entry data, at a Tar block start. When
	 * this method exits, it should be positioned at the next Tar block start,
	 * after the entry data.
	 * @param targetRoot The root directory of the extraction target directory
	 * hierarchy.
	 * @param spec The specification for the extraction operation.
	 * @throws WrappedIOException On I/O errors.
	 * @throws TarFileParseException If the extraction strategy does not know
	 * how to deal with the entry.
	 */
	public void extract(TarEntryHeaderData headerData, DataSource src, Directory targetRoot, TarExtractSpecification spec) throws WrappedIOException, TarFileParseException;
}
