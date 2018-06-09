package org.at4j.tar.builder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.at4j.archive.builder.AbstractArchiveBuilder;
import org.at4j.archive.builder.ArchiveEntryAddException;
import org.at4j.tar.TarConstants;
import org.entityfs.DataSink;
import org.entityfs.DirectoryView;
import org.entityfs.ReadableFile;
import org.entityfs.WritableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.lock.DummyLock;
import org.entityfs.lock.EntityLock;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.util.io.OutputStreamToDataSinkAdapter;

/**
 * This Tar builder writes its output to a stream. Apart from that, and that
 * data from streams cannot be added to it, it works just like the other
 * {@link TarBuilder}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarBuilder
 * @see org.at4j.tar.TarFile
 */
public class TarStreamBuilder extends AbstractArchiveBuilder<TarStreamBuilder, TarEntrySettings>
{
	private final DataSink m_out;
	private final boolean m_closeOutStreamWhenClosing;
	private final EntityLock m_targetWriteLock;
	private final TarEntryStrategy m_entryStrategy;

	private boolean m_closed = false;

	/**
	 * Create a Tar builder using the default settings. (See
	 * {@link TarBuilderSettings}.)
	 * @param f The file to write the archive to. The previous contents of this
	 * file is discarded. If this method completes successfully, the target file
	 * is locked for writing until {@link #close()} is called.
	 * @throws WrappedIOException On I/O errors.
	 * @see #TarStreamBuilder(WritableFile, TarBuilderSettings)
	 * @see #TarStreamBuilder(OutputStream, TarBuilderSettings)
	 */
	public TarStreamBuilder(WritableFile f) throws WrappedIOException
	{
		this(f, null);
	}

	/**
	 * Create a Tar builder using the supplied settings.
	 * @param f The file to write the archive to. The previous contents of this
	 * file is discarded. If this method completes successfully, the target file
	 * is locked for writing until {@link #close()} is called.
	 * @param settings The settings for the builder. Set this to {@code null} to
	 * use the default settings.
	 * @throws WrappedIOException On I/O errors.
	 * @see #TarStreamBuilder(WritableFile)
	 * @see #TarStreamBuilder(OutputStream, TarBuilderSettings)
	 */
	public TarStreamBuilder(WritableFile f, TarBuilderSettings settings) throws WrappedIOException
	{
		super(settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS.combineWith(settings.getDefaultFileEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS,
				settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS.combineWith(settings.getDefaultDirectoryEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS);

		// Null check
		f.getClass();

		m_entryStrategy = settings != null ? settings.getEntryStrategy() : TarBuilderSettings.DEFAULT_ENTRY_STRATEGY;
		m_closeOutStreamWhenClosing = true;
		boolean successful = false;
		OutputStream outStream = null;
		m_targetWriteLock = f.lockForWriting();
		try
		{
			outStream = new BufferedOutputStream(f.openForWrite());
			m_out = new OutputStreamToDataSinkAdapter(outStream);
			successful = true;
		}
		finally
		{
			if (!successful)
			{
				m_closed = true;
				m_targetWriteLock.unlock();
				if (outStream != null)
				{
					try
					{
						outStream.close();
					}
					catch (IOException e)
					{
						throw new WrappedIOException(e);
					}
				}
			}
		}
	}

	/**
	 * Create a new Tar archive builder on an already open
	 * {@link java.io.OutputStream} object. It will use the supplied settings
	 * object.
	 * @param os The output stream to write data to. This stream is <i>not</i>
	 * closed when {@link #close()} is called. The Tar builder assumes that it
	 * has exclusive access to this {@code OutputStream}.
	 * @param settings The settings for the builder. Set this to {@code null} to
	 * use the default settings.
	 * @throws WrappedIOException On I/O errors.
	 * @see #TarStreamBuilder(WritableFile, TarBuilderSettings)
	 */
	public TarStreamBuilder(OutputStream os, TarBuilderSettings settings) throws WrappedIOException
	{
		super(settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS.combineWith(settings.getDefaultFileEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS,
				settings != null ? TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS.combineWith(settings.getDefaultDirectoryEntrySettings()) : TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS);

		// Null check
		os.getClass();

		m_out = new OutputStreamToDataSinkAdapter(os);
		m_entryStrategy = settings != null ? settings.getEntryStrategy() : TarBuilderSettings.DEFAULT_ENTRY_STRATEGY;
		m_closeOutStreamWhenClosing = false;
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
		m_entryStrategy.writeDirectory(m_out, da, location, effectiveSettings, lastModified);
	}

	@Override
	protected void addFileCallback(AbsoluteLocation location, ReadableFile f, TarEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		m_entryStrategy.writeFile(m_out, f, location, effectiveSettings, lastModified);
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
					m_out.write(new byte[TarConstants.BLOCK_SIZE * 2]);
				}
				finally
				{
					if (m_closeOutStreamWhenClosing)
					{
						m_out.close();
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
