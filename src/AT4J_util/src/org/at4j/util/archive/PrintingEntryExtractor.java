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
package org.at4j.util.archive;

import java.io.PrintStream;

import org.at4j.archive.ArchiveDirectoryEntry;
import org.at4j.archive.ArchiveFileEntry;
import org.at4j.archive.ArchiveSymbolicLinkEntry;
import org.at4j.support.entityfs.PotentialDirectory;
import org.entityfs.Directory;

/**
 * This entry extraction strategy just prints the location and types of the
 * entries that it encounters to a {@link PrintStream} ({@code System.out}, for
 * instance). It does not extract anything.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class PrintingEntryExtractor implements EntryExtractionStrategy
{
	private final PrintStream m_out;

	/**
	 * Create a printing entry extractor that prints to {@code System.out}.
	 */
	public PrintingEntryExtractor()
	{
		m_out = System.out;
	}

	/**
	 * Create a printing entry extractor that prints to the supplied stream.
	 * @param out The stream to print to.
	 */
	public PrintingEntryExtractor(PrintStream out)
	{
		// Null check
		out.getClass();

		m_out = out;
	}

	public Directory extractDirectory(ArchiveDirectoryEntry<?, ?> dir, PotentialDirectory target, ExtractSpecification spec)
	{
		m_out.println("d " + dir.getLocation());
		return null;
	}

	public void postProcessDirectory(ArchiveDirectoryEntry<?, ?> dir, Directory target, ExtractSpecification spec)
	{
		// Nothing
	}
	
	public void extractFile(ArchiveFileEntry<?, ?> f, PotentialDirectory target, ExtractSpecification spec)
	{
		m_out.println("f " + f.getLocation());
	}

	public void extractSymbolicLink(ArchiveSymbolicLinkEntry<?, ?> l, PotentialDirectory target, ExtractSpecification spec)
	{
		m_out.println("l " + l.getLocation() + " -> " + l.getLinkTarget());
	}
}
