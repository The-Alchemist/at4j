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

import org.at4j.support.lang.At4JException;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ArchiveEntryAddException extends At4JException
{
	private static final long serialVersionUID = 1L;

	public ArchiveEntryAddException(String msg)
	{
		super(msg);
	}

	public ArchiveEntryAddException(Throwable t)
	{
		super(t);
	}

	public ArchiveEntryAddException(String msg, Throwable t)
	{
		super(msg, t);
	}
}