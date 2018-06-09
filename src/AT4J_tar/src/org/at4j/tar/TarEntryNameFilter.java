package org.at4j.tar;

import org.entityfs.support.filter.AbstractConvenientFilter;

/**
 * This filter matches Tar entries with a specific name.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarExtractor
 * @see TarExtractSpecification
 */
public final class TarEntryNameFilter extends AbstractConvenientFilter<TarEntryHeaderData> implements TarEntryHeaderDataFilter
{
	private final String m_name;
	private final boolean m_caseSensitive;

	/**
	 * Create a new case sensitive entry name filter.
	 * @param name The name to match.
	 */
	public TarEntryNameFilter(String name)
	{
		// Null check
		name.getClass();

		m_name = name;
		m_caseSensitive = true;
	}

	/**
	 * Create a new entry name filter.
	 * @param name The name to match.
	 * @param caseSensitive Should the filter be case sensitive?
	 */
	public TarEntryNameFilter(String name, boolean caseSensitive)
	{
		// Null check
		name.getClass();

		m_name = caseSensitive ? name : name.toLowerCase();
		m_caseSensitive = caseSensitive;
	}

	public boolean matches(TarEntryHeaderData object)
	{
		return m_caseSensitive ? m_name.equals(object.getLocation().getName()) : m_name.equals(object.getLocation().getName().toLowerCase());
	}
}
