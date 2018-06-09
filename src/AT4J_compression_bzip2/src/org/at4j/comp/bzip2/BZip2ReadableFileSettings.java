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
 * Settings used when creating a {@link BZip2ReadableFile} object.
 * <p>
 * When this object is created it contains the default settings. Change the
 * settings by calling setter methods.
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BZip2ReadableFileSettings implements Cloneable
{
	private int m_bufferSize = BZip2ReadableFile.BUFFER_SIZE_NOT_SET;
	private boolean m_useCommonsCompress;
	private LogAdapter m_logAdapter;

	/**
	 * Set the buffer size for the buffered input stream that the bzip2 input
	 * stream is opened on.
	 * @param bufferSize The size of the buffer for the buffered input stream
	 * that the bzip2 input stream is opened on. Set this to {@code -1} to use
	 * the default buffer size (8192 bytes).
	 * @return {@code this}
	 * @throws IllegalArgumentException
	 */
	public BZip2ReadableFileSettings setBufferSize(int bufferSize) throws IllegalArgumentException
	{
		if (bufferSize <= 0)
		{
			throw new IllegalArgumentException("Invalid buffer size " + bufferSize + ". It must be a positive integer");
		}
		m_bufferSize = bufferSize;
		return this;
	}

	public int getBufferSize()
	{
		return m_bufferSize;
	}

	/**
	 * Should the bzip2 input stream from Apache Commons Compress be used
	 * instead of At4J's implementation?
	 * @param b Should Apache Commons Compress' bzip2 implementation be used
	 * instead of At4J's
	 * @return {@code this}
	 */
	public BZip2ReadableFileSettings setUseCommonsCompress(boolean b)
	{
		m_useCommonsCompress = b;
		return this;
	}

	public boolean isUseCommonsCompress()
	{
		return m_useCommonsCompress;
	}

	/**
	 * Set a log adapter that the bzip2 input stream will use to log diagnostic
	 * output.
	 * @param la The log adapter.
	 * @return {@code this}
	 */
	public BZip2ReadableFileSettings setLogAdapter(LogAdapter la)
	{
		m_logAdapter = la;
		return this;
	}

	public LogAdapter getLogAdapter()
	{
		return m_logAdapter;
	}

	/**
	 * Get the settings for a {@link BZip2InputStream} based on the
	 * configuration of this object.
	 * @return Stream settings.
	 */
	public BZip2InputStreamSettings getInputStreamSettings()
	{
		BZip2InputStreamSettings res = new BZip2InputStreamSettings();
		res.setLogAdapter(m_logAdapter);
		return res;
	}

	/**
	 * Make a copy of this object.
	 */
	@Override
	public BZip2ReadableFileSettings clone()
	{
		try
		{
			return (BZip2ReadableFileSettings) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new At4JException("Bug", e);
		}
	}
}
