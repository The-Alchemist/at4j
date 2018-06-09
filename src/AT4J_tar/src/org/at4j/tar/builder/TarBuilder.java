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
package org.at4j.tar.builder;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import org.at4j.archive.builder.AbstractStreamAddCapableArchiveBuilder;
import org.at4j.archive.builder.ArchiveEntryAddException;
import org.at4j.tar.TarConstants;
import org.entityfs.DirectoryView;
import org.entityfs.RandomAccess;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.lock.DummyLock;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.RandomAccessMode;

/**
 * The Tar builder is an {@link org.at4j.archive.builder.ArchiveBuilder} for
 * building Tar files. Files and directories are written to the archive as they
 * are added using any of this object's {@code add} methods.
 * <p>
 * The Tar file format is fairly simple, but it has evolved a bit since it was
 * introduced. By default, this object creates Tar file compatible with the Gnu
 * Tar implementation. By setting another {@link TarEntryStrategy}, the archive
 * can be made to have a different format. The following strategy
 * implementations exist:
 * <ul>
 * <li>{@link V7TarEntryStrategy} &ndash; creates a Tar file using the old Unix
 * v7 Tar format. File names (including their paths) are limited to a maximum
 * length of 99 characters.</li>
 * <li>{@link UstarEntryStrategy} &ndash; creates a Tar file using the POSIX
 * 1003.1-1988 (ustar) format. File names (including their paths) are limited to
 * a theoretical maximum length of 255 characters. Depending on where there is a
 * slash in the file name, the actual maximum length is often shorter. The
 * shortest maximum length is 99 characters. Ustar entries have the entity owner
 * user and group names in text as well as their numerical values.</li>
 * <li>{@link GnuTarEntryStrategy} &ndash; (the default) creates a Tar file
 * using the same file format as the Gnu Tar (1.20) implementation. This
 * implementation extends the ustar format with support for file names of an
 * unlimited length. This implementation does not support all Gnu Tar features,
 * such as sparse files and archive labels.</li>
 * <li>{@link PaxTarEntryStrategy} &nsash; creates a Tar file using the POSIX
 * 1003.1-2001 (pax) format. This extends the ustar format with support for file
 * names of an unlimited length and with support for metadata variables.</li>
 * </ul>
 * All file formats are somewhat compatible with each other. If a tar
 * implementation does not understand a specific feature, it treats it as a
 * regular file. This may result in files with strange names appearing after
 * unpacking an archive.
 * <p>
 * This object uses the strategy described in
 * {@link org.at4j.archive.builder.ArchiveBuilder} to determine the effective
 * {@link TarEntrySettings} for an entry added to the archive.
 * <p>
 * This implementation does not support adding symbolic links to the archive.
 * <p>
 * If it is in a locking {@link org.entityfs.FileSystem}, the target file is
 * locked for writing until the Tar builder is {@link #close()}:d.
 * <p>
 * This object is <i>not</i> safe to use concurrently from several threads
 * without external synchronization.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarStreamBuilder
 * @see org.at4j.tar.TarFile
 */
public class TarBuilder extends AbstractStreamAddCapableArchiveBuilder<TarBuilder, TarEntrySettings>
{
	private final RandomAccess m_targetRandomAccess;
	private final boolean m_closeRandomAccessWhenClosing;
	private final Lock m_targetWriteLock;
	private final TarEntryStrategy m_entryStrategy;

	private boolean m_closed = false;

	/**
	 * Create a Tar builder using the default settings. (See
	 * {@link TarBuilderSettings}.)
	 * @param target The file to write the archive to. The previous contents of
	 * this file is discarded. If this method completes successfully, the target
	 * file is locked for writing until {@link #close()} is called.
	 * @throws WrappedIOException On I/O errors.
	 * @see #TarBuilder(RandomlyAccessibleFile, TarBuilderSettings)
	 * @see #TarBuilder(RandomAccess, TarBuilderSettings)
	 */
	public TarBuilder(RandomlyAccessibleFile target) throws WrappedIOException
	{
		this(target, null);
	}

	/**
	 * Create a Tar builder using the supplied settings.
	 * @param target The file to write the archive to. The previous contents of
	 * this file is discarded. If this method completes successfully, the target
	 * file is locked for writing until {@link #close()} is called.
	 * @param settings The settings for the builder. Set this to {@code null} to
	 * use the default settings.
	 * @throws WrappedIOException On I/O errors.
	 * @see #TarBuilder(RandomlyAccessibleFile)
	 * @see #TarBuilder(RandomAccess, TarBuilderSettings)
	 */
	public TarBuilder(RandomlyAccessibleFile target, TarBuilderSettings settings) throws WrappedIOException
	{
		super(settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS.combineWith(settings.getDefaultFileEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS,
				settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS.combineWith(settings.getDefaultDirectoryEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS);

		// Null check
		target.getClass();

		m_entryStrategy = settings != null ? settings.getEntryStrategy() : TarBuilderSettings.DEFAULT_ENTRY_STRATEGY;
		m_closeRandomAccessWhenClosing = true;

		RandomAccess targetRandomAccess = null;
		boolean successful = false;
		m_targetWriteLock = target.lockForWriting();
		try
		{
			targetRandomAccess = target.openForRandomAccess(RandomAccessMode.READ_WRITE);
			targetRandomAccess.setLength(0);
			successful = true;
			m_targetRandomAccess = targetRandomAccess;
		}
		finally
		{
			if (!successful)
			{
				m_closed = true;
				m_targetWriteLock.unlock();
				if (targetRandomAccess != null)
				{
					targetRandomAccess.close();
				}
			}
		}
	}

	/**
	 * Create a new Tar archive builder on an already open
	 * {@link org.entityfs.RandomAccess} object. It will use the supplied
	 * settings object.
	 * @param target The {@code RandomAccess} to write data to. This object is
	 * <i>not</i> closed when {@link #close()} is called. The Tar builder
	 * assumes that it has exclusive access to this {@code RandomAccess}.
	 * @param settings The settings for the builder. Set this to {@code null} to
	 * use the default settings.
	 * @throws WrappedIOException On I/O errors.
	 * @see #TarBuilder(RandomlyAccessibleFile, TarBuilderSettings)
	 */
	public TarBuilder(RandomAccess target, TarBuilderSettings settings) throws WrappedIOException
	{
		super(settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS.combineWith(settings.getDefaultFileEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS,
				settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS.combineWith(settings.getDefaultDirectoryEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS);

		// Null check
		target.getClass();

		m_entryStrategy = settings != null ? settings.getEntryStrategy() : TarBuilderSettings.DEFAULT_ENTRY_STRATEGY;
		m_closeRandomAccessWhenClosing = false;
		m_targetRandomAccess = target;
		m_targetWriteLock = DummyLock.INSTANCE;
	}

	@Override
	protected TarEntrySettings getDefaultDefaultDirectoryEntrySettings()
	{
		return TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS;
	}

	@Override
	protected TarEntrySettings getDefaultDefaultFileEntrySettings()
	{
		return TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS;
	}

	@Override
	protected void assertNotClosed() throws IllegalStateException
	{
		if (m_closed)
		{
			throw new IllegalStateException("This Tar builder is closed");
		}
	}

	@Override
	protected void addDirectoryCallback(AbsoluteLocation location, Object d, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		DirectoryAdapter<?> da;
		if (d instanceof DirectoryView)
		{
			da = new DirectoryViewDirectoryAdapter((DirectoryView) d);
		}
		else if (d instanceof File)
		{
			da = new FileDirectoryAdapter((File) d);
		}
		else
		{
			throw new ArchiveEntryAddException("Don't know how to adapt " + d + " to something that I can use");
		}
		m_entryStrategy.writeDirectory(m_targetRandomAccess, da, location, effectiveSettings, lastModified);
	}

	@Override
	protected void addFileCallback(AbsoluteLocation location, ReadableFile f, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		m_entryStrategy.writeFile(m_targetRandomAccess, f, location, effectiveSettings, lastModified);
	}

	@Override
	protected void addStreamCallback(AbsoluteLocation location, InputStream is, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		m_entryStrategy.writeFileFromStream(m_targetRandomAccess, is, location, effectiveSettings, lastModified);
	}

	public boolean isClosed()
	{
		return m_closed;
	}

	/**
	 * This method finishes writing the Tar file and closes it. It also releases
	 * the write lock on the created Tar file, if it is in a locking
	 * {@link org.entityfs.FileSystem}.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void close() throws WrappedIOException
	{
		if (!m_closed)
		{
			try
			{
				try
				{
					// Write two empty blocks
					// The array contents defaults to zeroes. Just what we want.
					m_targetRandomAccess.write(new byte[TarConstants.BLOCK_SIZE * 2]);
				}
				finally
				{
					if (m_closeRandomAccessWhenClosing)
					{
						m_targetRandomAccess.close();
					}
				}
			}
			finally
			{
				m_targetWriteLock.unlock();
				m_closed = true;
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (!m_closed)
		{
			close();
		}
		super.finalize();
	}
}
