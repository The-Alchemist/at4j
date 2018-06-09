package org.at4j.archive.builder;

import org.entityfs.support.filter.AbstractConvenientFilter;

/**
 * This filter matches entities with a specific name. The filter may either be
 * case sensitive or not.
 * <p>
 * "ETAF" stands for {@code EntryToArchiveFilter}
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class NameETAF extends AbstractConvenientFilter<EntryToArchive> implements EntityToArchiveFilter
{
	private final String m_name;
	private final boolean m_caseSensitive;

	/**
	 * Create a new case sensitive name filter.
	 * @param name The name to match.
	 */
	public NameETAF(String name)
	{
		this(name, true);
	}

	/**
	 * Create a new name filter.
	 * @param name The name to match.
	 * @param caseSensitive Should the filter be case sensitive?
	 */
	public NameETAF(String name, boolean caseSensitive)
	{
		// Null check
		name.getClass();

		m_name = caseSensitive ? name : name.toLowerCase();
		m_caseSensitive = caseSensitive;
	}

	public boolean matches(EntryToArchive object)
	{
		String name = object.getLocation().getName();
		return m_caseSensitive ? m_name.equals(name) : m_name.equals(name.toLowerCase());
	}
}
