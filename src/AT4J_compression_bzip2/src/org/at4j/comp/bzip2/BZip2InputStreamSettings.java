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
package org.at4j.comp.bzip2;

import org.at4j.support.lang.At4JException;
import org.entityfs.support.log.LogAdapter;

/**
 * This object contains settings for the {@link BZip2InputStream}.
 * <p>
 * When created, this object contains the default {@link BZip2InputStream}
 * settings. Settings are then modified by calling its setters.
 * @author Karl Gustafsson
 * @since 1.1
 * @see BZip2InputStream
 */
public class BZip2InputStreamSettings implements Cloneable
{
	private LogAdapter m_logAdapter;

	/**
	 * Set a {@link LogAdapter} for writing diagnostic output from the bzip2
	 * decoder to. Diagnostic output is written to the debug and trace levels.
	 * <p>
	 * By default, no log adapter is used. This disables diagnostic output
	 * altogether.
	 * @param la The log adapter.
	 * @return {@code this}
	 */
	public BZip2InputStreamSettings setLogAdapter(LogAdapter la)
	{
		m_logAdapter = la;
		return this;
	}

	/**
	 * Get the log adapter used to write diagnostic output to.
	 * @return The log adapter, or {@code null} if no log adapter is set.
	 */
	public LogAdapter getLogAdapter()
	{
		return m_logAdapter;
	}

	/**
	 * Make a copy of this object.
	 */
	@Override
	public BZip2InputStreamSettings clone()
	{
		try
		{
			return (BZip2InputStreamSettings) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new At4JException("Bug", e);
		}
	}
}
