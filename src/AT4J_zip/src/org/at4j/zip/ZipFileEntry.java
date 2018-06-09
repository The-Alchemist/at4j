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
package org.at4j.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ScatteringByteChannel;

import org.at4j.archive.ArchiveFileEntry;
import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.comp.UnsupportedCompressionMethodException;
import org.at4j.zip.comp.ZipEntryCompressionMethod;
import org.entityfs.RandomAccess;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.exception.ReadOnlyException;
import org.entityfs.lock.DummyLock;
import org.entityfs.lock.EntityLock;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.RandomAccessMode;
import org.entityfs.support.io.ScatteringByteChannelAdapter;
import org.entityfs.util.base.EmptyRandomAccess;

/**
 * This object represents a file entry in a Zip archive. In addition to the
 * properties inherited from the {@link ZipEntry} object, a file entry has some
 * properties of its own. The file's data is compressed using a
 * {@link ZipEntryCompressionMethod}. The file has a set of internal file
 * attributes represented by a {@link ZipInternalFileAttributes} object and the
 * entry keeps track of the compressed and the uncompressed size of the file
 * data, and the file has a CRC 32 checksum of its data.
 * <p>
 * Only the Zip file entries that are stored uncompressed (
 * {@link org.at4j.zip.comp.StoredCompressionMethod}) support being opened for
 * random access. If it is stored with another compression method, its {@code
 * openForRandomAccess} method will throw an
 * {@link UnsupportedOperationException}.
 * <p>
 * Zip entries are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipDirectoryEntry
 * @see ZipSymbolicLinkEntry
 */
public class ZipFileEntry extends ZipEntry implements ArchiveFileEntry<ZipEntry, ZipDirectoryEntry>
{
	private final long m_startPosOfFileData;
	private final ZipEntryCompressionMethod m_compressionMethod;
	private final UnsignedInteger m_uncompressedSize;
	private final UnsignedInteger m_compressedSize;
	private final UnsignedInteger m_crc32;
	private final ZipInternalFileAttributes m_internalFileAttributes;

	/**
	 * Create a new Zip file entry.
	 * @param collaborator The parent Zip archive's entry collaborator.
	 * @param zecd Data parsed from the file's record in the central directory.
	 * @param zeld Data parsed from the file entry's local header.
	 */
	public ZipFileEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld)
	{
		super(collaborator, zecd, zeld);
		m_compressionMethod = zecd.getCompressionMethod();
		m_startPosOfFileData = zeld.getStartPositionOfFileData();
		m_uncompressedSize = zecd.getUncompressedSize();
		m_compressedSize = zecd.getCompressedSize();
		m_crc32 = zecd.getCrc32();
		m_internalFileAttributes = zecd.getInternalFileAttributes();
	}

	/**
	 * Is the Zip file entry compressed patched data?
	 * @return {@code true} if the Zip file entry is compressed patched data.
	 */
	public boolean isCompressedPatchData()
	{
		return getGeneralPurposeBitFlags().isCompressedPatchedData();
	}

	/**
	 * Get the compression method for the Zip file entry.
	 * @return The compression method for the Zip file entry.
	 */
	public ZipEntryCompressionMethod getCompressionMethod()
	{
		return m_compressionMethod;
	}

	/**
	 * Get the uncompressed size of file data. This method returns the same
	 * value as {@link #getUncompressedSize()}.
	 * @return The size of uncompressed data, in bytes.
	 * @see #getUncompressedSize()
	 * @see #getSize()
	 */
	public long getDataSize()
	{
		return m_uncompressedSize.longValue();
	}

	/**
	 * Get the size of the file data when it has been uncompressed. This method
	 * returns the same value as {@link #getSize()}.
	 * @return The size of uncompressed data, in bytes.
	 * @see #getDataSize()
	 * @see #getCompressedSize()
	 */
	public UnsignedInteger getUncompressedSize()
	{
		return m_uncompressedSize;
	}

	/**
	 * Get the size of the file data when as it is compressed in the Zip file.
	 * This method returns the same value as {@link #getSize()}.
	 * @return The size of compressed data, in bytes.
	 * @see #getSize()
	 * @see #getUncompressedSize()
	 */
	public UnsignedInteger getCompressedSize()
	{
		return m_compressedSize;
	}

	/**
	 * Get the compressed size of the Zip file entry. This method returns the
	 * same value as {@link #getCompressedSize()}.
	 * @return The size of compressed data, in bytes.
	 * @see #getDataSize()
	 * @see #getCompressedSize()
	 */
	public long getSize()
	{
		return m_compressedSize.longValue();
	}

	/**
	 * Get the checksum calculated on the file contents.
	 * @return The checksum calculated on the file contents.
	 * @see java.util.zip.CRC32
	 */
	public UnsignedInteger getCrc32()
	{
		return m_crc32;
	}

	/**
	 * Does this file appear to be a text file?
	 * @return {@code true} if the file appears to be a text file, {@code false}
	 * if it appears to be a binary file.
	 */
	public boolean isAppearingToBeTextFile()
	{
		return m_internalFileAttributes.isTextFile();
	}

	/**
	 * This method returns a dummy lock.
	 * @return A dummy lock.
	 */
	public EntityLock lockForWriting() throws LockTimeoutException
	{
		return DummyLock.INSTANCE;
	}

	/**
	 * This method returns a dummy lock.
	 * @return A dummy lock.
	 */
	public EntityLock getWriteLock()
	{
		return DummyLock.INSTANCE;
	}

	/**
	 * This method always returns {@code true}.
	 * @return {@code true}, always.
	 */
	public boolean isWriteLockedByCurrentThread()
	{
		return true;
	}

	public ScatteringByteChannel openChannelForRead() throws UnsupportedOperationException
	{
		return new ScatteringByteChannelAdapter(Channels.newChannel(openForRead()));
	}

	public InputStream openForRead() throws UnsupportedCompressionMethodException, ZipFileParseException
	{
		if (m_compressedSize.intValue() == 0)
		{
			return new ByteArrayInputStream(new byte[0]);
		}
		else
		{
			try
			{
				boolean successful = false;
				InputStream is = getCollaborator().openStream(m_startPosOfFileData, m_startPosOfFileData + m_compressedSize.longValue() - 1);
				try
				{
					InputStream res = m_compressionMethod.createInputStream(is, m_compressedSize.longValue(), m_uncompressedSize.longValue());
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
				throw new ZipFileParseException(e);
			}
		}
	}

	public RandomAccess openForRandomAccess(RandomAccessMode ram) throws UnsupportedCompressionMethodException, UnsupportedOperationException, ZipFileParseException
	{
		if (ram != RandomAccessMode.READ_ONLY)
		{
			throw new ReadOnlyException("A Zip entry is read only");
		}

		if (!m_compressionMethod.isRandomAccessSupported())
		{
			throw new UnsupportedOperationException("Random access is not supported for files compressed with the " + m_compressionMethod + " compression method");
		}

		if (m_compressedSize.intValue() == 0)
		{
			return new EmptyRandomAccess();
		}
		else
		{
			try
			{
				boolean successful = false;
				RandomAccess ra = getCollaborator().openRandomAccess(m_startPosOfFileData, m_startPosOfFileData + m_compressedSize.longValue() - 1);
				try
				{
					RandomAccess res = m_compressionMethod.createRandomAccess(ra, m_compressedSize.longValue(), m_uncompressedSize.longValue());
					successful = true;
					return res;
				}
				finally
				{
					if (!successful)
					{
						ra.close();
					}
				}
			}
			catch (IOException e)
			{
				throw new WrappedIOException(e);
			}
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
