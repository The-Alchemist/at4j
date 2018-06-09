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

import java.nio.charset.Charset;

import org.entityfs.ostrat.DoOverwriteAndLogWarning;
import org.entityfs.ostrat.OverwriteStrategy;
import org.entityfs.support.filter.Filter;

/**
 * This specification object contains configuration for an extraction operation
 * by the {@link TarExtractor}.
 * <p>
 * When this object is instantiated, it contains the default extraction
 * settings:
 * <ul>
 * <li>No filter.</li>
 * <li>Do extract entries (
 * {@link org.at4j.tar.ExtractTarEntryExtractionStrategy}).</li>
 * <li>File names are encoded in the platform's default charset.</li>
 * <li>Existing files and directories are overwritten and a warning is printed
 * out.</li>
 * </ul>
 * @author Karl Gustafsson
 * @since 1.0
 */
public class TarExtractSpecification
{
	private Filter<? super TarEntryHeaderData> m_filter;
	private TarEntryExtractionStrategy m_entryExtractionStrategy = new ExtractTarEntryExtractionStrategy(false);
	private Charset m_fileNameCharset = Charset.defaultCharset();
	private OverwriteStrategy m_overwriteStrategy = DoOverwriteAndLogWarning.INSTANCE;

	/**
	 * Set the filter to use to decide which entries to extract. Set this to
	 * {@code null} to extract all entries.
	 * <p>
	 * All entries in the Tar file are evaluated against this filter. If it
	 * matches an entry but not its parent directory, the parent directory is
	 * extracted anyway.
	 * @param f The filter that is used to decide which entries to extract. Any
	 * {@link org.at4j.archive.ArchiveEntryFilter} works here.
	 */
	public TarExtractSpecification setFilter(Filter<? super TarEntryHeaderData> f)
	{
		m_filter = f;
		return this;
	}

	/**
	 * Get the filter that is used to decide which of the Tar entries to
	 * extract. If this is {@code null}, all entries are extracted.
	 * @return The filter or {@code null}.
	 */
	public Filter<? super TarEntryHeaderData> getFilter()
	{
		return m_filter;
	}

	/**
	 * Set the strategy object used for extracting entries.
	 * @param strat The strategy object.
	 */
	public TarExtractSpecification setEntryExtractionStrategy(TarEntryExtractionStrategy strat)
	{
		// Null check
		strat.getClass();

		m_entryExtractionStrategy = strat;
		return this;
	}

	/**
	 * Get the strategy object used for extracting entries.
	 * @return The strategy object used for extracting entries.
	 */
	public TarEntryExtractionStrategy getEntryExtractionStrategy()
	{
		return m_entryExtractionStrategy;
	}

	/**
	 * Set the charset to use for decoding file names in the Tar file (and the
	 * names of other entry types as well). By default, the platform's default
	 * charset is used.
	 * @param cs The charset.
	 */
	public TarExtractSpecification setFileNameCharset(Charset cs)
	{
		// Null check
		cs.getClass();

		m_fileNameCharset = cs;
		return this;
	}

	/**
	 * Get the charset used for decoding file names in the Tar file.
	 * @return The charset.
	 */
	public Charset getFileNameCharset()
	{
		return m_fileNameCharset;
	}

	/**
	 * If there already are an entity (file or directory) at the location where
	 * an entry should be extracted, should the old entity be overwritten?
	 * Should a warning message be printed?
	 * <p>
	 * Non-empty directories cannot be overwritten. If the Tar extractor
	 * attempts to do so, an {@link TarFileParseException} is thrown.
	 * @param os The strategy to use for overwriting entities.
	 * @return {@code this}
	 */
	public TarExtractSpecification setOverwriteStrategy(OverwriteStrategy os)
	{
		m_overwriteStrategy = os;
		return this;
	}

	/**
	 * Are existing entities overwritten with entries from the archive?
	 * @return The overwrite strategy
	 * @see #setOverwriteStrategy(OverwriteStrategy)
	 */
	public OverwriteStrategy getOverwriteStrategy()
	{
		return m_overwriteStrategy;
	}
}
