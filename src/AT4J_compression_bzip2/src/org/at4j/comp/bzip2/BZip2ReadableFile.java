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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.entityfs.ReadableFile;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.lock.EntityLock;
import org.entityfs.lock.ReadLockRequiredException;
import org.entityfs.security.AccessDeniedException;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.StreamUtil;

/**
 * This is a {@link ReadableFile} that transparently decompresses the contents
 * of a wrapped file using bzip2.
 * <p>
 * bzip2 input streams are opened on buffered input streams on the underlying
 * file. The buffer size can be configured.
 * <p>
 * A {@link ReadableFile} can be made into a
 * {@link org.entityfs.NamedReadableFile} using the
 * {@link org.entityfs.util.NamedReadableFileAdapter}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see BZip2WritableFile
 * @see org.at4j.comp.lzma.LzmaReadableFile
 * @see org.entityfs.util.io.GZipReadableFile
 * @see BZip2InputStream
 */
public class BZip2ReadableFile implements ReadableFile
{
	static final int BUFFER_SIZE_NOT_SET = -1;

	private final ReadableFile m_adapted;
	private final BZip2ReadableFileSettings m_settings;

	/**
	 * Create a new adapter. The adapter will assume that the bzip2 compressed
	 * data does <i>not</i> start with the two magic bytes "BZ".
	 * @param adapted The adapted file.
	 * @see #BZip2ReadableFile(ReadableFile, BZip2ReadableFileSettings)
	 */
	public BZip2ReadableFile(ReadableFile adapted)
	{
		this(adapted, new BZip2ReadableFileSettings());
	}

	/**
	 * Create a new adapter. The adapter will use the default buffer size of
	 * 8192 bytes.
	 * @param adapted The adapted file.
	 * @param assumeMagicBytes Does the compressed data in the file start with
	 * the two magic bytes "BZ"? Starting with At4J 1.1, this property must be
	 * {@code true}.
	 * @deprecated The {@code assumeMagicBytes} property is always true from
	 * At4J 1.1.
	 */
	@Deprecated
	public BZip2ReadableFile(ReadableFile adapted, boolean assumeMagicBytes)
	{
		this(adapted, assumeMagicBytes, BUFFER_SIZE_NOT_SET);
	}

	/**
	 * Create a new adapter.
	 * @param adapted The adapted file.
	 * @param assumeMagicBytes Does the compressed data in the file start with
	 * the two magic bytes "BZ"? Starting with At4J 1.1, this property must be
	 * {@code true}.
	 * @param bufferSize The size of the buffer for the buffered input stream
	 * that the bzip2 input stream is opened on. Set this to {@code -1} to use
	 * the default buffer size (8192 bytes).
	 * @deprecated The {@code assumeMagicBytes} property is always true from
	 * At4J 1.1.
	 */
	@Deprecated
	public BZip2ReadableFile(ReadableFile adapted, boolean assumeMagicBytes, int bufferSize)
	{
		if (!assumeMagicBytes)
		{
			throw new IllegalArgumentException("This class can no longer handle bzip2 streams that don't start with the magic bytes 'BZ'");
		}

		// Null check
		adapted.getClass();

		m_adapted = adapted;
		m_settings = new BZip2ReadableFileSettings();
		m_settings.setBufferSize(bufferSize);
	}

	/**
	 * Create a new adapter.
	 * @param adapted The adapted file.
	 * @param settings The settings for this readable file. A clone of the
	 * settings object is stored in the created object.
	 * @see #BZip2ReadableFile(ReadableFile)
	 * @since 1.1
	 */
	public BZip2ReadableFile(ReadableFile adapted, BZip2ReadableFileSettings settings)
	{
		// Null check
		adapted.getClass();

		m_adapted = adapted;
		m_settings = settings.clone();
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
			InputStream is = m_settings.getBufferSize() == BUFFER_SIZE_NOT_SET ? new BufferedInputStream(m_adapted.openForRead()) : new BufferedInputStream(m_adapted.openForRead(), m_settings.getBufferSize());
			try
			{
				InputStream res;
				if (m_settings.isUseCommonsCompress())
				{
					res = new BZip2CompressorInputStream(is);
				}
				else
				{
					res = new BZip2InputStream(is, m_settings.getInputStreamSettings());
				}
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
				// -2 == the bzip2 stream header
				return StreamUtil.getSizeOfDataInStream(is, 8192) - 2;
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
