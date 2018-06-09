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
package org.at4j.tar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ScatteringByteChannel;

import org.at4j.archive.ArchiveFileEntry;
import org.entityfs.RandomAccess;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.exception.ReadOnlyException;
import org.entityfs.lock.DummyLock;
import org.entityfs.lock.EntityLock;
import org.entityfs.support.io.RandomAccessMode;
import org.entityfs.support.io.ScatteringByteChannelAdapter;
import org.entityfs.util.base.EmptyRandomAccess;

/**
 * This object represents a Unix v7 Tar file entry. All Tar file entries support
 * being opened for random access.
 * <p>
 * Tar entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarDirectoryEntry
 * @see TarSymbolicLinkEntry
 */
public class TarFileEntry extends TarEntry implements ArchiveFileEntry<TarEntry, TarDirectoryEntry>
{
	private static final UnixEntityMode DEFAULT_ENTITY_MODE = UnixEntityMode.forCode(0644);

	private final long m_startPosOfFileData;
	private final long m_size;

	TarFileEntry(TarEntryHeaderData hd, long startPosOfFileData, TarEntryCollaborator collaborator)
	{
		super(hd, hd.getLocation(), collaborator);

		m_startPosOfFileData = startPosOfFileData;
		m_size = hd.getFileSize();
	}

	@Override
	protected UnixEntityMode getDefaultEntityMode()
	{
		return DEFAULT_ENTITY_MODE;
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

	/**
	 * Open a {@link ScatteringByteChannel} that a client can use to read file
	 * data from the Tar file entry.
	 * @return A channel that the client can read data from.
	 */
	public ScatteringByteChannel openChannelForRead()
	{
		return new ScatteringByteChannelAdapter(Channels.newChannel(openForRead()));
	}

	/**
	 * Open an {@link InputStream} that a client can use to read file data from
	 * the Tar file entry.
	 * @return An open input stream.
	 */
	public InputStream openForRead()
	{
		if (m_size == 0)
		{
			return new ByteArrayInputStream(new byte[0]);
		}
		else
		{
			return getCollaborator().openStream(m_startPosOfFileData, m_startPosOfFileData + m_size - 1);
		}
	}

	public RandomAccess openForRandomAccess(RandomAccessMode ram) throws ReadOnlyException
	{
		if (ram != RandomAccessMode.READ_ONLY)
		{
			throw new ReadOnlyException("A Tar entry is read only");
		}
		if (m_size == 0)
		{
			return new EmptyRandomAccess();
		}
		else
		{
			return getCollaborator().openRandomAccess(m_startPosOfFileData, m_startPosOfFileData + m_size - 1);
		}
	}

	/**
	 * Get the start position of file data in the Tar file for this Tar file
	 * entry.
	 * <p>
	 * Clients seldom have to bother with this.
	 * @return The start position of this file entry's data.
	 */
	public long getStartPosOfFileData()
	{
		return m_startPosOfFileData;
	}

	/**
	 * Get the size of the file data for this Tar file entry. This is a value
	 * between {@code 0} and {@code 8589934591} bytes (~ 8.6 Gb) (inclusive).
	 * <p>
	 * This method returns the same value as {@link #getSize()}.
	 * @return The size of the file data for this Tar file entry.
	 */
	public long getDataSize()
	{
		return m_size;
	}

	/**
	 * Get the size of the file data for this Tar file entry. This is a value
	 * between {@code 0} and {@code 8589934591} bytes (~ 8.6 Gb) (inclusive).
	 * <p>
	 * This method returns the same value as {@link #getDataSize()}.
	 * @return The size of the file data for this Tar file entry.
	 */
	public long getSize()
	{
		return m_size;
	}
}
