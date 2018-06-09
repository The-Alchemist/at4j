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

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.at4j.archive.Archive;
import org.entityfs.RandomAccess;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.RandomAccessMode;
import org.entityfs.util.io.ReadWritableFileAdapter;

/**
 * A {@code TarFile} is an {@link Archive} containing {@link TarEntry}:s loaded
 * from a file. Tar entries may be files, directories or symbolic links. They
 * contain metadata about the file system entity that was used to create the tar
 * entry such as its time of last modification and its owner user and group
 * id:s.
 * <p>
 * The Tar file format is fairly simple, but it has evolved through a few
 * versions since it was first defined. The following formats are supported by
 * this object:
 * <ul>
 * <li><i>Unix v7 version</i> &ndash; the original Tar file format. It supports
 * path names up to 99 characters long and only numerical group and user id:s.
 * Represented by {@link TarEntry} objects.</li>
 * <li><i>POSIX 1003.1-1988 (ustar) version</i> &ndash; supports path names up
 * to 255 characters long and text user and group id:s. Represented by
 * {@link UstarEntry} objects.</li>
 * <li><i>Gnu Tar version</i> &ndash; supports path names of unlimited length
 * and text user and group id:s. Special features such as sparse archive files
 * are not supported. Represented by {@link UstarEntry} objects.</li>
 * <li><i>POSIX 1003.1-2001 (pax) version</i> &ndash; supports path names of
 * unlimited length, text user and group id:s and any number of metadata
 * variables (PAX variables) for each entry. Represented by {@link PaxEntry}
 * objects.</li>
 * </ul>
 * <p>
 * The entries in a Tar archive are positioned in a directory hierarchy. Parent
 * directories of entries may be absent. In that case they are represented by
 * {@link TarDirectoryEntry} objects using default directory settings.
 * <p>
 * If the file used to create this object is in a locking
 * {@link org.entityfs.FileSystem}, it is locked for reading by this object
 * until it is {@link #close()}:d.
 * <p>
 * A {@code TarFile} object should be safe to use concurrently from several
 * parallel threads without any external synchronization.
 * <p>
 * <b>Note on character encodings:</b> By default, metadata about Tar entries is
 * encoded using the default character encoding of the platform where the tar
 * file is created. This makes it necessary for those reading Tar files to know
 * which character encoding that was used when the archive was created. This is
 * often, but not always, Codepage 437 on Windows or UTF-8 on Unix. The only
 * exception to this rule is the Pax variables that are always encoded using
 * UTF-8. If Tar entries in a Pax compatible archive contains non-ASCII
 * characters, the Tar entry path is (often) stored in the Pax header, making
 * the archive portable between different platforms.
 * <p>
 * The Tar file format is described well in the <a
 * href="http://www.gnu.org/software/tar/manual/">Gnu Tar manual</a>.
 * <p>
 * This object has a main method that, when run, prints out the contents of a
 * Tar file passed to it as an argument.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarExtractor
 */
public class TarFile implements Archive<TarEntry, TarDirectoryEntry>
{
	private final RandomlyAccessibleFile m_backingFile;
	private final Lock m_tarFileReadLock;
	private final Map<AbsoluteLocation, TarEntry> m_entries;
	private final TarDirectoryEntry m_rootEntry;
	private final TarEntryCollaborator m_entryCollaborator;

	private final AtomicBoolean m_closed;

	/**
	 * Create a new Tar file archive object that reads data from the supplied
	 * file. File names and other text information in the Tar file is
	 * interpreted using the platform's default charset.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is locked
	 * for reading by this method. The read lock is released when this object is
	 * {@link #close()}:d. If this method returns with an error, the file is not
	 * locked.
	 * @param f The Tar file.
	 * @see #TarFile(RandomlyAccessibleFile, Charset)
	 */
	public TarFile(RandomlyAccessibleFile f)
	{
		this(f, null);
	}

	/**
	 * Create a new Tar file archive object that reads data from the supplied
	 * file. Entry names and other text information in the Tar file is
	 * interpreted using the supplied charset.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is locked
	 * for reading by this method. The read lock is released when this object is
	 * {@link #close()}:d. If this method returns with an error, the file is not
	 * locked.
	 * @param f The Tar file.
	 * @param entryNameCharset The charset to use for interpreting text metadata
	 * in the Tar file.
	 * @see #TarFile(RandomlyAccessibleFile)
	 */
	public TarFile(RandomlyAccessibleFile f, Charset entryNameCharset)
	{
		// Null checks
		f.getClass();

		boolean successful = false;
		m_backingFile = f;
		m_tarFileReadLock = f.lockForReading();
		try
		{
			m_entryCollaborator = new TarEntryCollaborator(f, this);

			RandomAccess ra = f.openForRandomAccess(RandomAccessMode.READ_ONLY);
			try
			{
				TarFileContentsBuildingEntryHandlerDelegate del = new TarFileContentsBuildingEntryHandlerDelegate(m_entryCollaborator);
				TarFileParser.INSTANCE.parse(ra, entryNameCharset != null ? entryNameCharset : Charset.defaultCharset(), del);
				TarFileContents tfc = del.getContents();
				m_entries = Collections.unmodifiableMap(tfc.getEntryMap());
				m_rootEntry = tfc.getRootEntry();
				successful = true;
			}
			finally
			{
				ra.close();
			}
		}
		finally
		{
			if (!successful)
			{
				m_tarFileReadLock.unlock();
			}
		}
		m_closed = new AtomicBoolean(false);
	}

	/**
	 * This method throws an {@link IllegalStateException} if this object has
	 * been closed.
	 * @throws IllegalStateException If this object has been closed.
	 */
	protected void assertNotClosed() throws IllegalStateException
	{
		if (m_closed.get())
		{
			throw new IllegalStateException("This Zip file is closed");
		}
	}

	/**
	 * Get the root directory entry for the Tar file. The root directory entry
	 * is the entry that has the absolute location {@code /} in the Tar file.
	 * <p>
	 * This entry is never present in the Tar file itself, so the returned
	 * object is always a {@link TarDirectoryEntry} (and not some subclass of
	 * that object).
	 * @return The Tar archive's root directory entry.
	 */
	public TarDirectoryEntry getRootEntry()
	{
		assertNotClosed();
		return m_rootEntry;
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public void clear() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Does the Tar archive contain the specified key (which should be an
	 * {@link AbsoluteLocation} object)?
	 * @param key The key to search for.
	 * @return {@code true} if the supplied key is present in the Tar file.
	 * @throws IllegalStateException If the Tar file has been closed.
	 */
	public boolean containsKey(Object key) throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.containsKey(key);
	}

	/**
	 * Does the Tar archive contain the specified value (which should be some
	 * kind of {@link TarEntry} object)?
	 * @param value The value to search for.
	 * @return {@code true} if the Tar file contains the specified value.
	 * @throws IllegalStateException If the Tar file has been closed.
	 */
	public boolean containsValue(Object value) throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.containsValue(value);
	}

	/**
	 * Get a read only set containing the entries in the Tar archive.
	 * @return A read only set containing the Tar archive's entries.
	 * @throws IllegalStateException If the Tar archive has been closed.
	 */
	public Set<Map.Entry<AbsoluteLocation, TarEntry>> entrySet() throws IllegalStateException
	{
		assertNotClosed();
		// The map is unmodifiable
		return m_entries.entrySet();
	}

	/**
	 * Get the {@link TarEntry} stored at the specified absolute location in the
	 * Tar file.
	 * @param key The {@link AbsoluteLocation} where the Tar entry is stored.
	 * @return The Tar entry or {@code null} if no Tar entry is stored at the
	 * specified location.
	 * @throws IllegalStateException If the Tar archive has been closed.
	 */
	public TarEntry get(Object key) throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.get(key);
	}

	/**
	 * This method always returns {@code false} since a Tar archive always has
	 * its root directory entry.
	 * @return {@code false}, always.
	 * @throws IllegalStateException If the Tar archive has been closed.
	 */
	public boolean isEmpty() throws IllegalStateException
	{
		assertNotClosed();
		return false;
	}

	/**
	 * Get a read only set containing all the {@link AbsoluteLocation}:s where
	 * Tar entries are stored in the Tar file.
	 * @return A read only set containing all Tar entry positions.
	 * @throws IllegalStateException If the Tar archive has been closed.
	 */
	public Set<AbsoluteLocation> keySet() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.keySet();
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public TarEntry put(AbsoluteLocation key, TarEntry value) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public void putAll(Map<? extends AbsoluteLocation, ? extends TarEntry> m) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public TarEntry remove(Object key) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the number of Tar entries in the archive.
	 * @return The number of Tar entries in the archive.
	 * @throws IllegalStateException If the Tar archive has been closed.
	 */
	public int size() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.size();
	}

	/**
	 * Get a read only collection containing all {@link TarEntry} objects in the
	 * Tar file.
	 * @return A read only collection containing all Tar entries.
	 * @throws IllegalStateException If the Tar archive has been closed.
	 */
	public Collection<TarEntry> values() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.values();
	}
	
	/**
	 * Get the file that this object was created from.
	 * @since 1.2
	 */
	public RandomlyAccessibleFile getBackingFile()
	{
		return m_backingFile;
	}

	/**
	 * Close this Tar file. This closes all open streams on file entries in the
	 * Tar file and releases the read lock on the Tar file.
	 * <p>
	 * This method can safely be called several times.
	 */
	public void close()
	{
		if (!m_closed.getAndSet(true))
		{
			try
			{
				m_entryCollaborator.close();
			}
			finally
			{
				m_tarFileReadLock.unlock();
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}

	private static void printDirectory(TarDirectoryEntry de)
	{
		if (de.isEmpty())
		{
			System.out.println(de.getLocation().getLocation().substring(1) + "/");
		}
		else
		{
			for (TarEntry te : de.getChildEntries().values())
			{
				if (te instanceof TarFileEntry)
				{
					System.out.println(te.getLocation().getLocation().substring(1));
				}
				else if (te instanceof TarDirectoryEntry)
				{
					printDirectory((TarDirectoryEntry) te);
				}
				else if (te instanceof TarSymbolicLinkEntry)
				{
					System.out.println(te.getLocation().getLocation().substring(1) + " -> " + ((TarSymbolicLinkEntry) te).getLinkTarget());
				}
				else
				{
					System.err.println("Unknown entry type: " + te);
				}
			}
		}
	}

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.err.println("This class must be run with one and only one argument (the Tar file). Got " + args.length);
			System.exit(1);
		}

		try
		{
			TarFile tf = new TarFile(new ReadWritableFileAdapter(new File(args[0])));
			try
			{
				printDirectory(tf.getRootEntry());
			}
			finally
			{
				tf.close();
			}
		}
		catch (WrappedIOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
