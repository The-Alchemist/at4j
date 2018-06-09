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

import org.at4j.archive.ArchiveEntry;
import org.entityfs.ostrat.DoOverwriteAndLogWarning;
import org.entityfs.ostrat.OverwriteStrategy;
import org.entityfs.support.filter.Filter;

/**
 * This specification object contains configuration for an extraction operation
 * by the {@link ArchiveExtractor}.
 * <p>
 * When this object is instantiated, it contains the default extraction
 * settings:
 * <ul>
 * <li>No filter</li>
 * <li>Do extract entries ({@link EntryExtractor}).</li>
 * <li>Overwrite existing entities and print a warning.</li>
 * <li>Don't preserve modification times.</li>
 * </ul>
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ExtractSpecification
{
	private Filter<? super ArchiveEntry<?, ?>> m_filter;
	private EntryExtractionStrategy m_entryExtractionStrategy = EntryExtractor.INSTANCE;
	private OverwriteStrategy m_overwriteStrategy = DoOverwriteAndLogWarning.INSTANCE;
	private boolean m_preserveModificationTimes = false;

	/**
	 * Get the filter for the extraction operation.
	 * @return Get the filter, or {@code null} if no filter is set.
	 */
	public Filter<? super ArchiveEntry<?, ?>> getFilter()
	{
		return m_filter;
	}

	/**
	 * Set the filter that archive entries must match in order to be extracted.
	 * This filter is evaluated against all entries in the archive. If an entry,
	 * but not its parent directory, matches the filter, the parent directory is
	 * created at the target anyway. Archive entry filters often implement the
	 * marker interface
	 * @param filter The filter, or {@code null} if all entries should be
	 * extracted.
	 * @return {@code this}
	 */
	public ExtractSpecification setFilter(Filter<? super ArchiveEntry<?, ?>> filter)
	{
		m_filter = filter;
		return this;
	}

	/**
	 * Get the strategy object for extracting entries.
	 * @return The strategy object for extracting entries.
	 */
	public EntryExtractionStrategy getEntryExtractionStrategy()
	{
		return m_entryExtractionStrategy;
	}

	/**
	 * Set the strategy object for extracting entries. This will be called with
	 * all entries that match the filter (if any).
	 * @param entryExtractor The entry extraction strategy.
	 * @return {@code this}
	 */
	public ExtractSpecification setEntryExtractionStrategy(EntryExtractionStrategy entryExtractor)
	{
		// Null check
		entryExtractor.getClass();

		m_entryExtractionStrategy = entryExtractor;
		return this;
	}

	/**
	 * If an entity already exists in a location where the extractor tries to
	 * extract an entry, should the old entry be overwritten? Should a warning
	 * be printed?
	 * <p>
	 * Non-empty directories cannot be overwritten. If the archive extractor
	 * encounters a non-empty directory, it throws an
	 * {@link org.entityfs.exception.DirectoryNotEmptyException}.
	 * @param strat The overwrite strategy to use.
	 * @return {@code this}
	 */
	public ExtractSpecification setOverwriteStrategy(OverwriteStrategy strat)
	{
		m_overwriteStrategy = strat;
		return this;
	}

	public OverwriteStrategy getOverwriteStrategy()
	{
		return m_overwriteStrategy;
	}
	
	/**
	 * Should the modification times that are stored for files and, for Tar,
	 * directories be set on the extracted entities?
	 * @param b Should modification times be preserved from the archive?
	 * @return {@code this}
	 */
	public ExtractSpecification setPreserveModificationTimes(boolean b)
	{
		m_preserveModificationTimes = b;
		return this;
	}
	
	public boolean isPreserveModificationTimes()
	{
		return m_preserveModificationTimes;
	}
}
