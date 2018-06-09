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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.entityfs.ReadableFile;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.lock.EntityLock;
import org.entityfs.lock.ReadLockRequiredException;
import org.entityfs.security.AccessDeniedException;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.StreamUtil;

/**
 * This is a {@link ReadableFile} that transparently decompresses the contents
 * of a wrapped file using LZMA compression.
 * <p>
 * The LZMA stream is opened on a buffered input stream whose buffer size can be
 * configured.
 * <p>
 * A {@link ReadableFile} can be made into a
 * {@link org.entityfs.NamedReadableFile} using the
 * {@link org.entityfs.util.NamedReadableFileAdapter}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see LzmaWritableFile
 * @see org.entityfs.util.io.GZipReadableFile
 * @see org.at4j.comp.bzip2.BZip2ReadableFile
 */
public class LzmaReadableFile implements ReadableFile
{
	private static final int BUFFER_SIZE_NOT_SET = -1;

	private final ReadableFile m_adapted;
	private final int m_bufferSize;

	/**
	 * Create a new adapter. LZMA streams will be opened on buffered streams
	 * with the default {@link BufferedInputStream} buffer size.
	 * @param adapted The adapted file.
	 * @see #LzmaReadableFile(ReadableFile, int)
	 */
	public LzmaReadableFile(ReadableFile adapted)
	{
		// Null check
		adapted.getClass();

		m_adapted = adapted;
		m_bufferSize = BUFFER_SIZE_NOT_SET;
	}

	/**
	 * Create a new adapter. LZMA streams will be opened on buffered streams
	 * having the supplied buffer size.
	 * @param adapted The adapted file.
	 * @param bufSize The buffer size for buffered input streams.
	 * @throws IllegalArgumentException If the buffer size is {@code < 1}
	 */
	public LzmaReadableFile(ReadableFile adapted, int bufSize) throws IllegalArgumentException
	{
		// Null check
		adapted.getClass();

		if (bufSize <= 0)
		{
			throw new IllegalArgumentException("Invalid buffer size " + bufSize + ". It must be > 0");
		}

		m_adapted = adapted;
		m_bufferSize = bufSize;
	}

	public ReadableByteChannel openChannelForRead() throws ReadLockRequiredException, AccessDeniedException
	{
		return Channels.newChannel(openForRead());
	}

	public InputStream openForRead() throws ReadLockRequiredException, AccessDeniedException
	{
		try
		{
			boolean successful = false;
			InputStream is = m_bufferSize == BUFFER_SIZE_NOT_SET ? new BufferedInputStream(m_adapted.openForRead()) : new BufferedInputStream(m_adapted.openForRead(), m_bufferSize);
			try
			{
				InputStream res = new LzmaInputStream(is);
				successful = true;
				return res;
			}
			finally
			{
				if (!successful)
				{
					is.close();
				}
			}
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	public EntityLock getReadLock()
	{
		return m_adapted.getReadLock();
	}

	public boolean isReadLockedByCurrentThread() throws IllegalStateException
	{
		return m_adapted.isReadLockedByCurrentThread();
	}

	public EntityLock lockForReading() throws LockTimeoutException
	{
		return m_adapted.lockForReading();
	}

	public long getDataSize() throws ReadLockRequiredException, AccessDeniedException
	{
		try
		{
			InputStream is = openForRead();
			try
			{
				// Don't cache this value since it may change
				return StreamUtil.getSizeOfDataInStream(is, 8192);
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	public long getSize() throws ReadLockRequiredException, AccessDeniedException
	{
		return m_adapted.getSize();
	}
}
