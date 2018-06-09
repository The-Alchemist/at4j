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
import java.util.concurrent.locks.Lock;

import org.entityfs.DataSource;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.FileSystem;
import org.entityfs.ReadableFile;
import org.entityfs.exception.EntityNotFoundException;
import org.entityfs.exception.NotADirectoryException;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.util.FileReadableFile;
import org.entityfs.util.io.InputStreamToDataSourceAdapter;

/**
 * This object is used to extract entries from a Tar archive. It does the same
 * as the {@link org.at4j.util.archive.ArchiveExtractor}, but is a bit faster
 * since it does not have to parse the Tar file before extracting it. (It parses
 * the Tar file while extracting it.)
 * <p>
 * Java's file system support sets a limit to how much entry metadata the
 * extractor can extract. Currently, it only extracts the entry's last
 * modification time.
 * <p>
 * The extraction process can be fine-tuned by configuring a
 * {@link TarExtractSpecification} object and passing it to the extract method.
 * <p>
 * If the target is in a locking file system, the extractor automatically locks
 * files and directories as required.
 * <p>
 * This class has a runnable main method. When run, it prints out the contents
 * of a Tar file.
 * @author Karl Gustafsson
 * @since 1.0
 * @see org.at4j.util.archive.ArchiveExtractor
 */
public class TarExtractor
{
	private final ReadableFile m_tarFile;

	/**
	 * Create a new Tar extractor for the supplied file.
	 * <p>
	 * <i>Tip:</i> {@link org.entityfs.util.io.GZipReadableFile},
	 * {@link org.at4j.comp.bzip2.BZip2ReadableFile} and
	 * {@link org.at4j.comp.lzma.LzmaReadableFile} can all be used here.
	 * @param f The Tar file.
	 */
	public TarExtractor(ReadableFile f)
	{
		// Null check
		f.getClass();

		m_tarFile = f;
	}

	/**
	 * Create a new Tar extractor for the supplied file.
	 * @param f The Tar file.
	 */
	public TarExtractor(File f)
	{
		// Null check
		f.getClass();

		m_tarFile = new FileReadableFile(f);
	}

	protected void extractInternal(Directory target, TarExtractSpecification spec)
	{
		Lock rl = m_tarFile.lockForReading();
		try
		{
			DataSource src = new InputStreamToDataSourceAdapter(m_tarFile.openForRead());
			try
			{
				TarFileParser.INSTANCE.parse(src, spec.getFileNameCharset(), new TarExtractorEntryHandler(spec, target));
			}
			finally
			{
				src.close();
			}
		}
		finally
		{
			rl.unlock();
		}
	}

	/**
	 * Extract the contents of the Tar file into a directory hierarchy starting
	 * with the target directory. This method will use the default
	 * {@link TarExtractSpecification} configuration.
	 * @param target The target directory.
	 * @throws TarFileParseException On parse errors.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void extract(DirectoryView target) throws TarFileParseException, WrappedIOException
	{
		extractInternal((Directory) target.getViewedEntity(), new TarExtractSpecification());
	}

	/**
	 * Extract the contents of the Tar file into a directory hierarchy starting
	 * with the target directory.
	 * @param target The target directory. This may be null if {@code spec} is
	 * configured with a {@link TarEntryExtractionStrategy} that does not use a
	 * target directory.
	 * @param spec Configuration for the extraction operation.
	 * @throws TarFileParseException On parse errors.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void extract(DirectoryView target, TarExtractSpecification spec) throws TarFileParseException, WrappedIOException
	{
		extractInternal(target != null ? (Directory) target.getViewedEntity() : null, spec);
	}

	/**
	 * Extract the contents of the Tar file into a directory hierarchy starting
	 * with the target directory. This method will use the default
	 * {@link TarExtractSpecification} configuration.
	 * @param target The target directory.
	 * @throws TarFileParseException On parse errors.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the target directory does not exist.
	 * @throws NotADirectoryException If the target is not a directory.
	 */
	public void extract(File target) throws EntityNotFoundException, NotADirectoryException, TarFileParseException, WrappedIOException
	{
		extract(target, new TarExtractSpecification());
	}

	/**
	 * Extract the contents of the Tar file into a directory hierarchy starting
	 * with the target directory.
	 * @param target The target directory. This may be null if {@code spec} is
	 * configured with a {@link TarEntryExtractionStrategy} that does not use a
	 * target directory.
	 * @param spec Configuration for the extraction operation.
	 * @throws TarFileParseException On parse errors.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the target directory does not exist.
	 * @throws NotADirectoryException If the target is not a directory.
	 */
	public void extract(File target, TarExtractSpecification spec) throws EntityNotFoundException, NotADirectoryException, TarFileParseException, WrappedIOException
	{
		if (target == null)
		{
			extractInternal(null, spec);
		}
		else
		{
			if (!target.exists())
			{
				throw new EntityNotFoundException("The target directory " + target + " does not exist");
			}
			else if (!target.isDirectory())
			{
				throw new NotADirectoryException("The target " + target + " is not a directory");
			}

			FileSystem fs = new FSRWFileSystemBuilder().disableAccessControls().disableEntityValidityControls().setRoot(target).create();
			try
			{
				extractInternal(fs.getRootDirectory(), spec);
			}
			finally
			{
				fs.close();
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
			TarExtractSpecification spec = new TarExtractSpecification();
			spec.setEntryExtractionStrategy(new PrintTarEntryExtractionStrategy(System.out));
			new TarExtractor(new File(args[0])).extract((File) null, spec);
		}
		catch (WrappedIOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
