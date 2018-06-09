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

/**
 * This interface defines the settings for an entry that is being added to an
 * archive. It does not specify what settings, or properties, there are &ndash;
 * that is archive type-dependent &ndash; only the common operations for all
 * settings objects.
 * <p>
 * Every property in a settings object have two states. It can either be set
 * (i.e: non-{@code null}), or not set (i.e: {@code null}).
 * <p>
 * Different settings objects can be combined using the
 * {@link #combineWith(ArchiveEntrySettings)} method. That is used a lot by
 * {@link ArchiveBuilder}:s.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface ArchiveEntrySettings<T extends ArchiveEntrySettings<T>> extends Cloneable
{
	/**
	 * Create a new settings object that contains this object's settings
	 * combined with the settings from the supplied object.
	 * <p>
	 * The returned object is created by first cloning this object, and then by
	 * replacing all the property values for properties <i>that are set</i> in
	 * the supplied object with the values from that object. In other words,
	 * properties from the supplied object take precedence over properties from
	 * this object.
	 * @param settings The other settings object.
	 * @return A new settings object with the settings from this object combined
	 * with the settings from the supplied object.
	 */
	T combineWith(T settings);

	/**
	 * Set this settings object to be read only to prevent accidental
	 * modification.
	 * <p>
	 * After calling this method, a call to any of the object's setter methods
	 * will result in an {@link IllegalStateException}.
	 * <p>
	 * This method can safely be called several times.
	 * @return {@code this}.
	 */
	T setReadOnly();

	/**
	 * Clone this settings object.
	 * @return A clone of this settings object.
	 */
	T clone();
}
