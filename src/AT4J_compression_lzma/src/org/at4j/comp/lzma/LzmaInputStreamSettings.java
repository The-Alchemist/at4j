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
package org.at4j.comp.lzma;

import java.util.concurrent.ThreadFactory;

/**
 * This object contains configuration that decides how a {@link LzmaInputStream}
 * behaves. When new:ed, this object contains the default settings.
 * @author Karl Gustafsson
 * @since 1.0
 * @see LzmaInputStream
 */
public class LzmaInputStreamSettings
{
	/**
	 * By default, the stream tries to read the uncompressed size of the data
	 * after reading the decoder properties. See
	 * {@link #setReadUncompressedSize(boolean)}.
	 */
	public static final boolean DEFAULT_READ_UNCOMPRESSED_SIZE = true;

	/**
	 * The default maximum depth of the incoming data queue. See
	 * {@link #setMaxDataQueueDepth(int)}.
	 */
	public static final int DEFAULT_MAX_DATA_QUEUE_DEPTH = 1;

	private boolean m_readUncompressedSize = DEFAULT_READ_UNCOMPRESSED_SIZE;
	private long m_uncompressedSize = -1;
	private byte[] m_properties = null;
	private int m_maxDataQueueDepth = DEFAULT_MAX_DATA_QUEUE_DEPTH;
	private ThreadFactory m_threadFactory;

	/**
	 * Should the size of the data in the stream when uncompressed be read after
	 * the encoding settings header has been read? If the uncompressed size can
	 * be determined in some other way, this may be omitted from the stream.
	 * <p>
	 * The default behavior is to read the data size.
	 * <p>
	 * Even if the uncompressed data size is set with
	 * {@link #setUncompressedSize(long)}, the input stream will try to read the
	 * size anyway if this is set to {@code true}. In this case, the read size
	 * is discarded and the size from {@link #setUncompressedSize(long)} is
	 * used.
	 * @param b Should the data size be read?
	 * @return {@code this}
	 * @see #setUncompressedSize(long)
	 */
	public LzmaInputStreamSettings setReadUncompressedSize(boolean b)
	{
		m_readUncompressedSize = b;
		return this;
	}

	/**
	 * Should the uncompressed size of data in the stream be read after the
	 * stream properties header?
	 * @return {@code true} if the uncompressed size of data in the stream is
	 * read after the stream properties header.
	 * @see #setReadUncompressedSize(boolean)
	 */
	public boolean isReadUncompressedSize()
	{
		return m_readUncompressedSize;
	}

	/**
	 * Set the uncompressed size of the data that can be read from the stream.
	 * By default, this is set to {@code -1} (unknown).
	 * @param size The size of uncompressed data in the stream.
	 * @return {@code this}.
	 * @throws IllegalArgumentException If the size is less than {@code -1}.
	 * @see #setReadUncompressedSize(boolean)
	 */
	public LzmaInputStreamSettings setUncompressedSize(long size) throws IllegalArgumentException
	{
		if (size < -1)
		{
			throw new IllegalArgumentException("Invalid uncompressed data size " + size);
		}
		m_uncompressedSize = size;
		return this;
	}

	/**
	 * Get the size of uncompressed data in the stream.
	 * @return The size of uncompressed data in the stream, or {@code -1} if
	 * that is not known beforehand.
	 * @see #setUncompressedSize(long)
	 */
	public long getUncompressedSize()
	{
		return m_uncompressedSize;
	}

	/**
	 * Set the stream decoder properties (five bytes). By default, the input
	 * stream tries to read the decoder properties from the head of the stream
	 * (the first five bytes). If this property is set, it does not, and uses
	 * the properties set here instead.
	 * @param barr The decoder properties.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the property array is not five bytes
	 * long.
	 */
	public LzmaInputStreamSettings setProperties(byte[] barr) throws IllegalArgumentException
	{
		if (barr.length != 5)
		{
			throw new IllegalArgumentException("Invalid size of decoder properties: " + barr.length + " bytes. It must be five bytes long");
		}
		// Defensive copy
		m_properties = new byte[5];
		System.arraycopy(barr, 0, m_properties, 0, 5);
		return this;
	}

	/**
	 * Get the decoder properties or {@code null} if they are not known
	 * beforehand.
	 * @return The decoder properties.
	 * @see #setProperties(byte[])
	 */
	public byte[] getProperties()
	{
		if (m_properties == null)
		{
			return null;
		}
		else
		{
			// Defensive copy
			assert m_properties.length == 5;
			byte[] res = new byte[5];
			System.arraycopy(m_properties, 0, res, 0, 5);
			return res;
		}
	}

	/**
	 * Set the maximum depth of the incoming data queue. The incoming data queue
	 * holds data that the LZMA decoder has decoded before it is read from the
	 * input stream. Each entry in the queue is about the size of the decoder's
	 * dictionary (large), so it makes sense to keep the maximum depth low.
	 * <p>
	 * The default value of this is 1. Setting it to 2 may give a slightly
	 * faster decompression at the expense of a higher memory usage.
	 * @param depth The maximum depth of the incoming data queue.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the depth is less than 1.
	 */
	public LzmaInputStreamSettings setMaxDataQueueDepth(int depth) throws IllegalArgumentException
	{
		if (depth < 1)
		{
			throw new IllegalArgumentException("Invalid maximum data queue depth " + depth + ". It must be at least 1");
		}
		m_maxDataQueueDepth = depth;
		return this;
	}

	/**
	 * Get the maximum depth of the incoming data queue.
	 * @return The maximum depth of the incoming data queue.
	 * @see #setMaxDataQueueDepth(int)
	 */
	public int getMaxDataQueueDepth()
	{
		return m_maxDataQueueDepth;
	}

	/**
	 * Set the thread factory to use for creating the thread that will run the
	 * LZMA decompression.
	 * <p>
	 * If this is not set, the {@link LzmaInputStream} will just create a thread
	 * with {@code new Thread}.
	 * <p>
	 * By default, this property is {@code null}.
	 * @param tf The thread factory, or {@code null} if the
	 * {@link LzmaInputStream} should use {@code new Thread} to create the
	 * decompression thread.
	 * @return {@code this}
	 */
	public LzmaInputStreamSettings setThreadFactory(ThreadFactory tf)
	{
		m_threadFactory = tf;
		return this;
	}

	/**
	 * Get the thread factory that will be used for creating the LZMA
	 * decompression thread.
	 * @return The thread factory, or {@code null} if not set.
	 */
	public ThreadFactory getThreadFactory()
	{
		return m_threadFactory;
	}
}
