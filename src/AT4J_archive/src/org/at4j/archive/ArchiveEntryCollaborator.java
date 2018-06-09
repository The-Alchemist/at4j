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
package org.at4j.archive;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.entityfs.RandomAccess;
import org.entityfs.RandomAccessCloseObserver;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.io.RandomAccessMode;
import org.entityfs.util.io.RangeInputStream;
import org.entityfs.util.io.RangeRandomAccess;

/**
 * This is a collaborator object that gives archive entries access to the
 * archive.
 * <p>
 * This object should be safe to use concurrently from several threads. If the
 * archive file is in a locking {@link org.entityfs.FileSystem}, clients should
 * obtain read locks on the archive file manually for other threads than the one
 * creating the {@link Archive} object.
 * <p>
 * <b>Note:</b> This object is part of the archive implementation. Clients don't
 * have to bother with this.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <T> The type of entries in the archive.
 * @param <U> The type of directory entries in the archive.
 */
public abstract class ArchiveEntryCollaborator<T extends ArchiveEntry<T, U>, U extends ArchiveDirectoryEntry<T, U>> implements RandomAccessCloseObserver
{
	// This is read locked while this object lives
	private final RandomlyAccessibleFile m_archiveFile;
	private final Map<AbsoluteLocation, T> m_entryMap;
	private final Set<RandomAccess> m_openStreams = new HashSet<RandomAccess>();
	private boolean m_closed = false;

	/**
	 * Create a new collaborator.
	 * @param archiveFile The archive file. This file has to be locked by
	 * reading while this object is alive. If several threads access the archive
	 * file, all threads must own a read lock on the archive file (if it is in a
	 * locking file system).
	 * @param entryMap A map containing all of the entries in the archive. This
	 * object will use the {@code Map} instance that is supplied in the argument
	 * (i.e. not make a defensive copy of it). The map does not have to contain
	 * all archive entries when this object is created, but it must do so before
	 * any client starts to use the archive object.
	 */
	protected ArchiveEntryCollaborator(RandomlyAccessibleFile archiveFile, Map<AbsoluteLocation, T> entryMap)
	{
		// Null checks
		archiveFile.getClass();
		entryMap.getClass();

		m_archiveFile = archiveFile;
		m_entryMap = entryMap;
	}

	private RandomAccess createRandomAccess()
	{
		RandomAccess ra = m_archiveFile.openForRandomAccess(RandomAccessMode.READ_ONLY);
		ra.addCloseObserver(this);
		synchronized (m_openStreams)
		{
			m_openStreams.add(ra);
		}
		return ra;
	}

	/**
	 * Open an {@link InputStream} for reading the data for a file entry.
	 * @param pos The file's data's start position in the archive.
	 * @param upperBound The file's data's last position in the archive.
	 * @return An {@link InputStream} for reading data between the boundaries.
	 */
	public InputStream openStream(long pos, long upperBound)
	{
		return new RangeInputStream(createRandomAccess(), pos, upperBound);
	}

	/**
	 * Open a read only {@link RandomAccess} object that reads data for a file
	 * entry.
	 * @param pos The start position of the file data.
	 * @param upperBound The last position of the file data in the archive.
	 * @return A {@link RandomAccess} for reading data between the boundaries.
	 */
	public RandomAccess openRandomAccess(long pos, long upperBound)
	{
		return new RangeRandomAccess(createRandomAccess(), pos, upperBound);
	}

	/**
	 * Get the archive entry stored at the specified absolute location in the
	 * archive.
	 * @param loc The entry's position in the archive.
	 * @return An archive entry or {@code null} if there is no entry at the
	 * specified location.
	 */
	public T getEntry(AbsoluteLocation loc)
	{
		return m_entryMap.get(loc);
	}

	/**
	 * This callback method is called when a stream is closed on a file entry.
	 */
	public void notifyClosed(RandomAccess ra)
	{
		synchronized (m_openStreams)
		{
			m_openStreams.remove(ra);
		}
	}

	/**
	 * Close the collaborator. This method closes all open streams on file
	 * entries in the archive.
	 */
	public void close()
	{
		if (!m_closed)
		{
			m_closed = true;
			synchronized (m_openStreams)
			{
				for (RandomAccess ra : new ArrayList<RandomAccess>(m_openStreams))
				{
					// The callback from close removes the stream from the list
					// of open streams.
					ra.close();
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
}
