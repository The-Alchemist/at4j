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

import java.util.Date;
import java.util.Map;

import org.entityfs.el.AbsoluteLocation;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
class PaxVariableForTestProvider implements PaxVariableProvider
{
	public void addVariables(Map<String, String> variables, Object entity, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		if ("f1.txt".equals(location.getName()))
		{
			variables.put("is_f1.txt", "trueåäö");
		}
	}
}