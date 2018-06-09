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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.at4j.comp.CompressionLevel;
import org.entityfs.WritableFile;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.exception.ReadOnlyException;
import org.entityfs.lock.EntityLock;
import org.entityfs.lock.WriteLockRequiredException;
import org.entityfs.security.AccessDeniedException;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This is a {@link WritableFile} that transparently compresses the data written
 * to a file using bzip2 compression.
 * <p>
 * The bzip2 output stream is opened on a buffered output stream on the backing
 * file. The size of that buffer can be configured.
 * <p>
 * This file cannot be appended to. The {@code openForAppend} methods throw an
 * {@link UnsupportedOperationException}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see BZip2ReadableFile
 * @see org.entityfs.util.io.GZipWritableFile
 * @see org.at4j.comp.lzma.LzmaWritableFile
 * @see BZip2OutputStream
 */
public class BZip2WritableFile implements WritableFile
{
	static final int BUFFER_SIZE_NOT_SET = -1;

	private final WritableFile m_adapted;
	private final BZip2WritableFileSettings m_settings;

	private static int getBlockSize(CompressionLevel level)
	{
		switch (level)
		{
			case BEST:
				return 9;
			case DEFAULT:
				return BZip2OutputStreamSettings.DEFAULT_BLOCK_SIZE;
			case FASTEST:
				return 1;
			default:
				throw new RuntimeException("Unknown compression level. This is a bug.");
		}
	}

	/**
	 * Create a new adapter using the default bzip2 block size (900kb).
	 * <p>
	 * <i>Note:</i> Starting with At4J 1.1, the written bzip2 data <i>will</i>
	 * be prefixed with the bzip2 stream header "BZ".
	 * @param adapted The writable file to adapt.
	 */
	public BZip2WritableFile(WritableFile adapted)
	{
		this(adapted, new BZip2WritableFileSettings());
	}

	/**
	 * Create a new adapter using the supplied compression level.
	 * <p>
	 * <i>Note:</i> Starting with At4J 1.1, the written bzip2 data <i>will</i>
	 * be prefixed with the bzip2 stream header "BZ".
	 * @param adapted The writable file to adapt.
	 * @param level The compression level.
	 * @since 1.0.2
	 */
	public BZip2WritableFile(WritableFile adapted, CompressionLevel level)
	{
		this(adapted, new BZip2WritableFileSettings().setBlockSize(getBlockSize(level)));
	}

	/**
	 * Create a new adapter using the supplied compression level.
	 * @param adapted The writable file to adapt.
	 * @param level The compression level.
	 * @param writeMagicBytes Should the written data be prefixed by the two
	 * magic bytes, "BZ"? Starting with At4J 1.1, this property <i>must</i> be
	 * {@code true}.
	 * @since 1.0.2
	 * @deprecated The magic bytes are always written starting from At4J 1.1.
	 */
	@Deprecated
	public BZip2WritableFile(WritableFile adapted, CompressionLevel level, boolean writeMagicBytes)
	{
		this(adapted, getBlockSize(level), writeMagicBytes, BUFFER_SIZE_NOT_SET);
	}

	/**
	 * Create a new adapter using the supplied bzip2 buffer size.
	 * @param adapted The writable file to adapt.
	 * @param blockSize The size of bzip2 dictionary in 100k units. This must be
	 * a value between {@code 1} and {@code 9} (inclusive). A higher value gives
	 * a higher compression, but it will also make the compression and future
	 * decompressions use more memory.
	 * @param writeMagicBytes Should the written data be prefixed by the two
	 * magic bytes, "BZ"? Starting with At4J 1.1, this property <i>must</i> be
	 * {@code true}.
	 * @param bufferSize The size of the memory buffer used for buffering the
	 * written output before it is compressed.
	 * @throws IllegalArgumentException If the block size is less than {@code 1}
	 * or greater than {@code 10}, or if the buffer size is less than {@code 1}.
	 * @deprecated The magic bytes are always written starting from At4J 1.1.
	 */
	@Deprecated
	public BZip2WritableFile(WritableFile adapted, int blockSize, boolean writeMagicBytes, int bufferSize) throws IllegalArgumentException
	{
		if (!writeMagicBytes)
		{
			throw new IllegalArgumentException("Starting with At4J version 1.1, the bzip2 stream header bytes 'BZ' must be written");
		}

		// Null check
		adapted.getClass();
		if ((blockSize < 1 || blockSize > 9))
		{
			throw new IllegalArgumentException("Invalid block size " + blockSize + ". It must be 1 <= blockSize <= 9");
		}
		if ((bufferSize < 1) && (bufferSize != BUFFER_SIZE_NOT_SET))
		{
			throw new IllegalArgumentException("Invalid buffer size " + bufferSize + ". It must be > 0");
		}

		m_adapted = adapted;
		m_settings = new BZip2WritableFileSettings();
		m_settings.setBlockSize(blockSize);
		m_settings.setBufferSize(bufferSize);
	}

	/**
	 * Create a new adapter using the supplied bzip2 buffer size.
	 * @param adapted The writable file to adapt.
	 * @param settings Settings. A clone of the settings object is stored in the
	 * created object.
	 * @throws IllegalArgumentException If the block size is less than {@code 1}
	 * or greater than {@code 10}, or if the buffer size is less than {@code 1}.
	 * @since 1.1
	 */
	public BZip2WritableFile(WritableFile adapted, BZip2WritableFileSettings settings) throws IllegalArgumentException
	{
		// Null checks
		adapted.getClass();
		settings.getClass();

		m_adapted = adapted;
		m_settings = settings.clone();
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException Always.
	 */
	public WritableByteChannel openChannelForAppend() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Appending to bzip2 compressed files is not supported");
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
		throw new UnsupportedOperationException("Appending to bzip2 compressed files is not supported");
	}

	public OutputStream openForWrite() throws WriteLockRequiredException, AccessDeniedException, ReadOnlyException
	{
		OutputStream os = m_settings.getBufferSize() == BUFFER_SIZE_NOT_SET ? m_adapted.openForWrite() : new BufferedOutputStream(m_adapted.openForWrite(), m_settings.getBufferSize());
		try
		{
			boolean successful = false;
			try
			{
				OutputStream bos;
				if (m_settings.isUseCommonsCompress())
				{
					bos = new BZip2CompressorOutputStream(os, m_settings.getBlockSize());
				}
				else
				{
					bos = new BZip2OutputStream(os, m_settings.getOutputStreamSettings());
				}
				successful = true;
				return bos;
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
