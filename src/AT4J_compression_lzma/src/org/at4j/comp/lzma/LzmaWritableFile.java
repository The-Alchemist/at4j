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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.entityfs.WritableFile;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.exception.ReadOnlyException;
import org.entityfs.lock.EntityLock;
import org.entityfs.lock.WriteLockRequiredException;
import org.entityfs.security.AccessDeniedException;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This is a {@link WritableFile} that transparently compresses the data written
 * to a file using LZMA compression. The LZMA output stream is opened on a
 * buffered output stream on the file. The buffer size can be configured.
 * <p>
 * This file cannot be appended to. The {@code openForAppend} methods throw an
 * {@link UnsupportedOperationException}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see LzmaReadableFile
 * @see org.entityfs.util.io.GZipWritableFile
 * @see org.at4j.comp.bzip2.BZip2WritableFile
 */
public class LzmaWritableFile implements WritableFile
{
	private final WritableFile m_adapted;
	private final LzmaOutputStreamSettings m_settings;

	/**
	 * Create a new adapter using the default LZMA compression settings. LZMA
	 * output streams will be opened on buffered output streams using
	 * {@link BufferedOutputStream}'s default buffer size.
	 * @param adapted The writable file to adapt.
	 */
	public LzmaWritableFile(WritableFile adapted)
	{
		// Null check
		adapted.getClass();

		m_adapted = adapted;
		m_settings = null;
	}

	/**
	 * Create a new adapter using the supplied LZMA settings
	 * @param adapted The writable file to adapt.
	 * @param settings Compression configuration. Set this to {@code null} to
	 * use the default configuration.
	 */
	public LzmaWritableFile(WritableFile adapted, LzmaOutputStreamSettings settings)
	{
		// Null check
		adapted.getClass();

		m_adapted = adapted;
		m_settings = settings;
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException Always.
	 */
	public WritableByteChannel openChannelForAppend() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Appending to LZMA compressed files is not supported");
	}

	public WritableByteChannel openChannelForWrite() throws WriteLockRequiredException, AccessDeniedException, ReadOnlyException
	{
		return Channels.newChannel(openForWrite());
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException Always.
	 */
	public OutputStream openForAppend() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Appending to LZMA compressed files is not supported");
	}

	public OutputStream openForWrite() throws WriteLockRequiredException, AccessDeniedException, ReadOnlyException
	{
		try
		{
			boolean successful = false;
			OutputStream os = (m_settings == null) || (m_settings.getBufferSize() == LzmaOutputStreamSettings.BUFFER_SIZE_NOT_SET) ? new BufferedOutputStream(m_adapted.openForWrite()) : new BufferedOutputStream(m_adapted.openForWrite(),
					m_settings.getBufferSize());
			try
			{
				LzmaOutputStream los = m_settings != null ? new LzmaOutputStream(os, m_settings) : new LzmaOutputStream(os);
				successful = true;
				return los;
			}
			finally
			{
				if (!successful)
				{
					os.close();
				}
			}
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	public EntityLock getWriteLock()
	{
		return m_adapted.getWriteLock();
	}

	public boolean isWriteLockedByCurrentThread()
	{
		return m_adapted.isWriteLockedByCurrentThread();
	}

	public EntityLock lockForWriting() throws LockTimeoutException
	{
		return m_adapted.lockForWriting();
	}
}
