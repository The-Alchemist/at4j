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

import java.util.Map;

/**
 * This interface defines a Tar entry created with a POSIX 1003.1-2001 (pax)
 * compatible version of the Tar software.
 * <p>
 * Entries of this type (often) has a set of metadata variables (PAX variables).
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface PaxEntry extends UstarEntry
{
	/**
	 * Get a read only map containing this entry's PAX variables.
	 * @return A map containing this entry's PAX variables.
	 */
	Map<String, String> getPaxVariables();
}
