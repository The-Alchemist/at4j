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
package org.at4j.tar.builder;

/**
 * This interface adapts a directory entity of some kind to an interface that
 * the different {@link TarEntryStrategy} implementations can use.
 * <p>
 * Currently, this interface does not contain any methods except for the
 * {@link #getAdapted()} method. Other methods may be added here in the future.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface DirectoryAdapter<T>
{
	/**
	 * This method is used by the {@link TarEntryStrategy} to get the adapted
	 * directory representation if no other method defined in this interface (of
	 * which there currently are none) fits its needs.
	 * @return The adapted directory entity.
	 */
	T getAdapted();
}
