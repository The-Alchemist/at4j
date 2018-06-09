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

/**
 * A {@code ZipFile} is an {@link Archive} containing {@link ZipEntry}:s loaded
 * from a file. Zip entries may be files, directories or symbolic links. They
 * contain metadata about the file system entity that was used to create the Zip
 * entry. For more information on the metadata, see the {@link ZipEntry}
 * documentation.
 * <p>
 * The Zip file consists of several <i>local files</i>, each containing a local
 * file header and file data (for file entries), as well as a <i>central
 * directory</i> that contains data on all entries in the archive. The metadata
 * for one entry is partly stored in the local file header and partly in its
 * central directory entry.
 * <p>
 * The Zip file may also contain a text comment.
 * <p>
 * When the Zip file is opened, a {@link ZipFileParser} object is used to
 * interpret its contents. That object's configuration may be tweaked to
 * understand new types of Zip files.
 * <p>
 * The entries in a Zip archive are positioned in a directory hierarchy. Parent
 * directories of entries may be absent. In that case they are represented by
 * {@link ZipDirectoryEntry} objects using default directory settings.
 * <p>
 * If the file used to create this object is in a locking
 * {@link org.entityfs.FileSystem}, it is locked for reading by this object
 * until it is {@link #close()}:d.
 * <p>
 * A {@code ZipFile} object should be safe to use concurrently from several
 * parallel threads without any external synchronization.
 * <p>
 * This implementation does not support Zip file encryption or signatures, Zip
 * archives that span several files or the Zip64 format.
 * <p>
 * <b>Note on character encoding:</b> It is not specified in either PK-Zip's nor
 * Info-Zip's documentation (see below) which character encoding to use when
 * encoding text data in a Zip file. Windows programs (7-Zip, WinZip) use
 * Codepage 437 to encode file names, and the platform's default charset (often
 * Codepage 1252) for other text information such as entry comments. Unix
 * programs use the platform's default character encoding (often UTF-8) for both
 * file names and other text data.
 * <p>
 * The Zip file format is specified in <a
 * href="http://www.pkware.com/support/zip-application-note">PK-Zip's
 * application notes</a> and <a href="http://www.info-zip.org/doc/">Info-Zip's
 * application notes</a>.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntry
 * @see ZipFileParser
 * @see org.at4j.zip.builder.ZipBuilder
 */
public class ZipFile implements Archive<ZipEntry, ZipDirectoryEntry>
{
	private final RandomlyAccessibleFile m_backingFile;
	private final Lock m_zipFileReadLock;
	private final Map<AbsoluteLocation, ZipEntry> m_entries;
	private final ZipDirectoryEntry m_rootEntry;
	private final ZipEntryCollaborator m_entryCollaborator;
	private final String m_comment;
	private final AtomicBoolean m_closed;

	/**
	 * Create a new Zip file archive object that reads data from the supplied
	 * file. File names and other text information in the Zip file is
	 * interpreted using the platform's default charset. The Zip file contents
	 * is parsed using a standard {@link ZipFileParser} with the default
	 * settings.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is locked
	 * for reading by this method. The read lock is released when this object is
	 * {@link #close()}:d. If this method returns with an error, the file is not
	 * locked.
	 * <p>
	 * See the note on character encodings in the class documentation above.
	 * @param f The Zip file.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ZipFileParseException If the Zip file cannot be parsed for some
	 * other reason than an I/O error.
	 * @see #ZipFile(RandomlyAccessibleFile, Charset, Charset)
	 * @see #ZipFile(RandomlyAccessibleFile, Charset, Charset, ZipFileParser)
	 */
	public ZipFile(RandomlyAccessibleFile f) throws ZipFileParseException, WrappedIOException
	{
		this(f, Charset.defaultCharset(), Charset.defaultCharset(), new ZipFileParser());
	}

	/**
	 * Create a new Zip file archive object that reads data from the supplied
	 * file. File names and other text information in the Zip file is
	 * interpreted using the supplied charsets. The Zip file contents is parsed
	 * using a standard {@link ZipFileParser} with the default settings.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is locked
	 * for reading by this method. The read lock is released when this object is
	 * {@link #close()}:d. If this method returns with an error, the file is not
	 * locked.
	 * <p>
	 * See the note on character encodings in the class documentation above.
	 * @param f The Zip file.
	 * @param fileNameEncodingCs The charset to use for interpreting file names
	 * in the Zip file.
	 * @param textEncodingCs The charset to use for interpreting text
	 * information (other than file names) in the Zip file.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ZipFileParseException If the Zip file cannot be parsed for some
	 * other reason than an I/O error.
	 * @see #ZipFile(RandomlyAccessibleFile)
	 * @see #ZipFile(RandomlyAccessibleFile, Charset, Charset, ZipFileParser)
	 */
	public ZipFile(RandomlyAccessibleFile f, Charset fileNameEncodingCs, Charset textEncodingCs) throws ZipFileParseException, WrappedIOException
	{
		this(f, fileNameEncodingCs, textEncodingCs, new ZipFileParser());
	}

	/**
	 * Create a new Zip file archive object that reads data from the supplied
	 * file. File names and other text information in the Zip file is
	 * interpreted using the supplied charsets. The Zip file contents is parsed
	 * using the supplied {@link ZipFileParser}.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is locked
	 * for reading by this method. The read lock is released when this object is
	 * {@link #close()}:d. If this method returns with an error, the file is not
	 * locked.
	 * <p>
	 * See the note on character encodings in the class documentation above.
	 * @param f The Zip file.
	 * @param fileNameEncodingCs The charset to use for interpreting file names
	 * in the Zip file.
	 * @param textEncodingCs The charset to use for interpreting text
	 * information (other than file names) in the Zip file.
	 * @param zfp The Zip file parser.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ZipFileParseException If the Zip file cannot be parsed for some
	 * other reason than an I/O error.
	 * @see #ZipFile(RandomlyAccessibleFile)
	 * @see #ZipFile(RandomlyAccessibleFile, Charset, Charset)
	 */
	@SuppressWarnings("unchecked")
	public ZipFile(RandomlyAccessibleFile f, Charset fileNameEncodingCs, Charset textEncodingCs, ZipFileParser zfp) throws ZipFileParseException, WrappedIOException
	{
		// Null checks
		f.getClass();
		zfp.getClass();
		fileNameEncodingCs.getClass();
		textEncodingCs.getClass();

		boolean successful = false;
		m_backingFile = f;
		m_zipFileReadLock = f.lockForReading();
		try
		{
			m_entryCollaborator = new ZipEntryCollaborator(f, this);
			RandomAccess ra = f.openForRandomAccess(RandomAccessMode.READ_ONLY);
			try
			{
				if (ra.length() == 0)
				{
					// Empty file. Create a file with just an empty root entry
					m_rootEntry = new ZipDirectoryEntry(m_entryCollaborator, AbsoluteLocation.ROOT_DIR, Collections.EMPTY_MAP);
					m_entries = Collections.singletonMap(AbsoluteLocation.ROOT_DIR, (ZipEntry) m_rootEntry);
					m_comment = "";
				}
				else
				{
					// Non-empty file
					ZipFileContents eam = zfp.parse(m_entryCollaborator, ra, fileNameEncodingCs, textEncodingCs);
					m_entries = Collections.unmodifiableMap(eam.getEntryMap());
					m_rootEntry = eam.getRootEntry();
					m_comment = eam.getComment();
					successful = true;
				}
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
				m_zipFileReadLock.unlock();
			}
		}
		m_closed = new AtomicBoolean(false);
	}

	/**
	 * This method throws an {@link IllegalStateException} if the Zip object has
	 * been closed.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	protected void assertNotClosed() throws IllegalStateException
	{
		if (m_closed.get())
		{
			throw new IllegalStateException("This Zip file is closed");
		}
	}

	/**
	 * Get the Zip file's root entry. The root entry has the location {@code /}
	 * in the Zip archive.
	 * @return The Zip file's root entry.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public ZipDirectoryEntry getRootEntry() throws IllegalStateException
	{
		assertNotClosed();
		return m_rootEntry;
	}

	/**
	 * Get the Zip file's comment.
	 * @return The Zip file's comment. If the comment is not set, this method
	 * returns an empty string.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public String getComment() throws IllegalStateException
	{
		assertNotClosed();
		return m_comment;
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public void clear() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Does the Zip file contain an entry at the specified absolute location?
	 * @param key The {@link AbsoluteLocation}.
	 * @return {@code true} if the Zip file contains an entry at the specified
	 * absolute location.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public boolean containsKey(Object key) throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.containsKey(key);
	}

	/**
	 * Does the Zip file contain the specified Zip entry?
	 * @param value A {@link ZipEntry}.
	 * @return {@code true} if the Zip file contains the specified Zip entry.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public boolean containsValue(Object value) throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.containsValue(value);
	}

	/**
	 * Get a read only set containing all Zip entries and their locations in the
	 * Zip file.
	 * @return A read only set containing all Zip entries and their locations in
	 * the Zip file.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public Set<Map.Entry<AbsoluteLocation, ZipEntry>> entrySet() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.entrySet();
	}

	/**
	 * Get the Zip entry at the specified absolute location in the Zip file.
	 * @param key An {@link AbsoluteLocation}.
	 * @return The {@link ZipEntry} at the specified location, or {@code null}
	 * if there is no Zip entry at the location.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public ZipEntry get(Object key) throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.get(key);
	}

	/**
	 * This method always returns {@code false} since a Zip file always contains
	 * the root entry.
	 * @return {@code false}, always.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public boolean isEmpty() throws IllegalStateException
	{
		assertNotClosed();
		return false;
	}

	/**
	 * Get a read only set containing all absolute locations where there are Zip
	 * entries in this Zip file.
	 * @return A read only set containing all absolute locations where there are
	 * Zip entries in this Zip file.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public Set<AbsoluteLocation> keySet() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.keySet();
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public ZipEntry put(AbsoluteLocation key, ZipEntry value) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public void putAll(Map<? extends AbsoluteLocation, ? extends ZipEntry> m) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException Always.
	 */
	public ZipEntry remove(Object key) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the number of Zip entries in this Zip file
	 * @return The number of Zip entries in this Zip file.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public int size() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.size();
	}

	/**
	 * Get a read only collection containing all Zip entries in this Zip file
	 * @return A read only collection containing all Zip entries in this Zip
	 * file.
	 * @throws IllegalStateException If the Zip object has been closed.
	 */
	public Collection<ZipEntry> values() throws IllegalStateException
	{
		assertNotClosed();
		return m_entries.values();
	}

	/**
	 * This closes the Zip archive, all open input streams on Zip file entries
	 * in the archive, and releases the read lock on the Zip file.
	 * <p>
	 * It is safe to call this method several times on the same object.
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
				m_zipFileReadLock.unlock();
			}
		}
	}
	
	/**
	 * Get the file that this {@link ZipFile} was created from.
	 * @since 1.2
	 */
	public RandomlyAccessibleFile getBackingFile()
	{
		return m_backingFile;
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
}
