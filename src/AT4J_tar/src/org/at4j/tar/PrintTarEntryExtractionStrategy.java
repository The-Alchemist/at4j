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

import java.io.PrintStream;

import org.entityfs.DataSource;
import org.entityfs.Directory;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This extraction strategy can be used with the {@link TarExtractor} to just
 * print the contents of the Tar file.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarExtractor
 * @see TarExtractSpecification
 */
public class PrintTarEntryExtractionStrategy implements TarEntryExtractionStrategy
{
	private final PrintStream m_out;

	/**
	 * Create a printing strategy that will print to the supplied stream.
	 * @param out The stream to print to.
	 */
	public PrintTarEntryExtractionStrategy(PrintStream out)
	{
		// Null check
		out.getClass();

		m_out = out;
	}

	public void extract(TarEntryHeaderData headerData, DataSource src, Directory targetRoot, TarExtractSpecification spec) throws WrappedIOException, TarFileParseException
	{
		m_out.println(headerData.getLocation().getLocation().substring(1));

		if (headerData.getFileSize() > 0)
		{
			// Seek forward to the next block boundary
			src.skipBytes((((headerData.getFileSize() - 1) / TarConstants.BLOCK_SIZE) + 1) * TarConstants.BLOCK_SIZE);
		}
	}
}
