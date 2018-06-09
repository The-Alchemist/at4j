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
package org.at4j.util.archive;

import java.io.File;
import java.util.Map;

import org.at4j.archive.Archive;
import org.at4j.archive.ArchiveDirectoryEntry;
import org.at4j.archive.ArchiveEntry;
import org.at4j.archive.ArchiveFileEntry;
import org.at4j.archive.ArchiveSymbolicLinkEntry;
import org.at4j.support.entityfs.ExistingDirectory;
import org.at4j.support.entityfs.FutureDirectory;
import org.at4j.support.entityfs.PotentialDirectory;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.FileSystem;
import org.entityfs.exception.EntityNotFoundException;
import org.entityfs.exception.NotADirectoryException;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.filter.Filter;

/**
 * This object can be used to extract some or all of the entries from an archive
 * to a directory. No metadata is copied to the extracted entries. Symbolic
 * links are not supported and they will be ignored.
 * <p>
 * An extraction operation is configured with an optional
 * {@link ExtractSpecification} object. It can be used to fine-tune the
 * extraction process.
 * @author Karl Gustafsson
 * @since 1.0
 * @see org.at4j.tar.TarExtractor
 */
public class ArchiveExtractor
{
	private final Archive<?, ?> m_archive;

	/**
	 * Create a new archive extractor that extracts contents from the supplied
	 * archive.
	 * @param archive The archive to extract contents from.
	 */
	public ArchiveExtractor(Archive<?, ?> archive)
	{
		// Null check
		archive.getClass();

		m_archive = archive;
	}

	private void extractDirectory(ArchiveDirectoryEntry<?, ?> dir, PotentialDirectory target, ExtractSpecification spec)
	{
		Filter<? super ArchiveEntry<?, ?>> filter = spec.getFilter();
		for (Map.Entry<String, ?> me : dir.getChildEntries().entrySet())
		{
			ArchiveEntry<?, ?> entry = (ArchiveEntry<?, ?>) me.getValue();
			PotentialDirectory thisDir = null;
			boolean filterMatches = false;
			if (filter == null || filter.matches(entry))
			{
				filterMatches = true;
				if (entry instanceof ArchiveFileEntry<?, ?>)
				{
					spec.getEntryExtractionStrategy().extractFile((ArchiveFileEntry<?, ?>) entry, target, spec);
				}
				else if (entry instanceof ArchiveDirectoryEntry<?, ?>)
				{
					Directory td = spec.getEntryExtractionStrategy().extractDirectory((ArchiveDirectoryEntry<?, ?>) entry, target, spec);
					if (td != null)
					{
						thisDir = new ExistingDirectory(td);
					}
				}
				else if (entry instanceof ArchiveSymbolicLinkEntry<?, ?>)
				{
					spec.getEntryExtractionStrategy().extractSymbolicLink((ArchiveSymbolicLinkEntry<?, ?>) entry, target, spec);
				}
				else
				{
					System.err.println("Don't know how to extract the entry " + entry + ". This entry will be ignored!");
				}
			}

			if (entry instanceof ArchiveDirectoryEntry<?, ?>)
			{
				// Proceed down into the directory
				if (!filterMatches)
				{
					thisDir = new FutureDirectory(target, entry.getName(), spec.getOverwriteStrategy(), System.currentTimeMillis());
				}
	
				// thisDir will be null if we tried to create the directory
				// but failed.
				if (thisDir != null) 
				{
					extractDirectory((ArchiveDirectoryEntry<?, ?>) entry, thisDir, spec);
					
					if (filterMatches)
					{
						// Post-process the created directory now that we have
						// extracted all of its child entries. This can be used to
						// set the last modification time of the directory.
						spec.getEntryExtractionStrategy().postProcessDirectory((ArchiveDirectoryEntry<?, ?>) entry, ((ExistingDirectory) thisDir).getDirectory(), spec);
					}
				}
			}
		}
	}

	/**
	 * Extract all entries in the archive to the supplied directory.
	 * @param target The target directory. If this is a view, all its view
	 * settings will be ignored.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void extract(DirectoryView target) throws WrappedIOException
	{
		extractDirectory(m_archive.getRootEntry(), new ExistingDirectory((Directory) target.getViewedEntity()), new ExtractSpecification());
	}

	/**
	 * Extract the archive entries that match the supplied filter to the target
	 * directory.
	 * @param target The target directory. If this is a view, all its view
	 * settings will be ignored.
	 * @param spec The specification object that contains configuration for this
	 * extraction. {@link org.at4j.archive.ArchiveEntryFilter}.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void extract(DirectoryView target, ExtractSpecification spec) throws WrappedIOException
	{
		extractDirectory(m_archive.getRootEntry(), new ExistingDirectory((Directory) target.getViewedEntity()), spec);
	}

	private FileSystem getTargetFileSystem(File f) throws EntityNotFoundException, NotADirectoryException
	{
		if (!f.exists())
		{
			throw new EntityNotFoundException("The target directory " + f + " does not exist");
		}
		else if (!f.isDirectory())
		{
			throw new NotADirectoryException("The target " + f + " is not a directory");
		}

		return new FSRWFileSystemBuilder().disableAccessControls().disableEntityValidityControls().setRoot(f).create();
	}

	/**
	 * Extract all entries in the archive to the supplied directory.
	 * @param target The target directory. This must be an existing directory.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the target directory does not exist.
	 * @throws NotADirectoryException If the target exists but is not a
	 * directory.
	 */
	public void extract(File target) throws WrappedIOException, EntityNotFoundException, NotADirectoryException
	{
		FileSystem targetFs = getTargetFileSystem(target);
		try
		{
			extractDirectory(m_archive.getRootEntry(), new ExistingDirectory(targetFs.getRootDirectory()), new ExtractSpecification());
		}
		finally
		{
			targetFs.close();
		}
	}

	/**
	 * Extract the archive entries that match the supplied filter to the target
	 * directory.
	 * @param target The target directory. This must be an existing directory.
	 * @param spec The specification object that contains configuration for this
	 * extraction.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the target directory does not exist.
	 * @throws NotADirectoryException If the target exists but is not a
	 * directory.
	 */
	public void extract(File target, ExtractSpecification spec) throws WrappedIOException, EntityNotFoundException, NotADirectoryException
	{
		FileSystem targetFs = getTargetFileSystem(target);
		try
		{
			extractDirectory(m_archive.getRootEntry(), new ExistingDirectory(targetFs.getRootDirectory()), spec);
		}
		finally
		{
			targetFs.close();
		}
	}
}
