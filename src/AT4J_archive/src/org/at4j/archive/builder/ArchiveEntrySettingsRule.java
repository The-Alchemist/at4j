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

import org.entityfs.support.filter.Filter;

/**
 * This is a rule for setting archive entry settings for one or several entries.
 * A rule is an {@link ArchiveEntrySettings} object with a
 * {@link EntityToArchiveFilter} do decide which entries that it will apply to.
 * <p>
 * A rule can have different scopes. If it is added as a global rule to the
 * {@link ArchiveBuilder} its scope is all entries added to the builder from
 * that point in time on. If it is used in in any of the builder's {@code
 * addRecursively} methods, its scope is all entries added by that method call.
 * <p>
 * The settings from this rule is combined with default settings and settings
 * from other rules as is described in the {@link ArchiveBuilder} documentation.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <T> The type of settings.
 * @see ArchiveBuilder
 */
public class ArchiveEntrySettingsRule<T extends ArchiveEntrySettings<T>>
{
	private final Filter<EntryToArchive> m_filter;
	private final T m_settings;

	/**
	 * Create a new rule.
	 * @param settings The entry settings for the rule.
	 * @param filter The filter that is used to determine which entries this
	 * rule will apply to. If this is {@code null}, the rule will apply to all
	 * entries that is within its scope. This kind of filters often implements
	 * the marker interface {@link EntityToArchiveFilter}.
	 */
	public ArchiveEntrySettingsRule(T settings, Filter<EntryToArchive> filter)
	{
		// Null check
		settings.getClass();

		m_settings = settings;
		m_filter = filter;
	}

	/**
	 * Get the entry settings for this rule.
	 * @return The entry settings for this rule.
	 */
	public T getSettings()
	{
		return m_settings;
	}

	/**
	 * Get the filter that determines which entries that this rule will apply
	 * to. This kind of filters often implements the marker interface
	 * {@link EntityToArchiveFilter}.
	 * @return The filter, or {@code null} if this rule should apply to all
	 * entries within its scope.
	 */
	public Filter<EntryToArchive> getFilter()
	{
		return m_filter;
	}
}
