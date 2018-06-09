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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import org.entityfs.DataSource;
import org.entityfs.Directory;
import org.entityfs.EFile;
import org.entityfs.ETDirectory;
import org.entityfs.ETFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ostrat.OverwriteResult;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.Directories;
import org.entityfs.util.io.DataSourceToInputStreamAdapter;

/**
 * This strategy object extract Tar entries to a target directory. It is the
 * default entry extraction strategy used by the {@link TarExtractor}.
 * <p>
 * The object can be configured to ignore symbolic link entries. If symbolic
 * links are not ignore, and this object gets a symbolic link entry, it throws a
 * {@link TarFileParseException}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarExtractSpecification
 */
public class ExtractTarEntryExtractionStrategy implements TarEntryExtractionStrategy
{
	private final boolean m_ignoreSymbolicLinks;

	public ExtractTarEntryExtractionStrategy(boolean ignoreSymbolicLinks)
	{
		m_ignoreSymbolicLinks = ignoreSymbolicLinks;
	}

	protected boolean isIgnoreSymbolicLinks()
	{
		return m_ignoreSymbolicLinks;
	}

	protected Directory getDirectory(Directory targetRoot, AbsoluteLocation loc)
	{
		return Directories.putIfAbsentDirectory(targetRoot, loc.getRelativeTo(AbsoluteLocation.ROOT_DIR));
	}

	protected void skipToNextBlockBoundary(DataSource src, long leastNoToForwardFromLast)
	{
		// Skip to the next Tar block boundary
		if (leastNoToForwardFromLast > 0)
		{
			src.skipBytes((((leastNoToForwardFromLast - 1) / TarConstants.BLOCK_SIZE) + 1) * TarConstants.BLOCK_SIZE);
		}
	}

	protected void extractDirectory(TarEntryHeaderData headerData, DataSource src, Directory targetRoot, TarExtractSpecification spec)
	{
		String name = headerData.getLocation().getName();
		Directory parentDir = getDirectory(targetRoot, headerData.getLocation().getParentLocation());
		Lock wl = parentDir.lockForWriting();
		try
		{
			OverwriteResult ores = spec.getOverwriteStrategy().overwrite(parentDir, name, ETDirectory.TYPE);
			if (ores == OverwriteResult.CAN_CREATE_NEW_ENTITY)
			{
				long parentLastModified = parentDir.getLastModified();
				Directory dir = (Directory) parentDir.newEntity(ETDirectory.TYPE, name, null);
				Lock dwl = dir.lockForWriting();
				try
				{
					dir.setLastModified(headerData.getLastModificationTime().getTime());
				}
				finally
				{
					dwl.unlock();
				}
				parentDir.setLastModified(parentLastModified);
			}
			else if (ores == OverwriteResult.KEPT_OLD_DIRECTORY)
			{
				parentDir.getEntityOrNull(name).setLastModified(headerData.getLastModificationTime().getTime());
			}
		}
		finally
		{
			wl.unlock();
		}

		// The size of the data is probably 0, so mostly we don't have to skip
		// anything.
		skipToNextBlockBoundary(src, headerData.getFileSize());
	}

	protected void extractFile(TarEntryHeaderData headerData, DataSource src, Directory targetRoot, TarExtractSpecification spec)
	{
		String name = headerData.getLocation().getName();
		Directory parentDir = getDirectory(targetRoot, headerData.getLocation().getParentLocation());
		Lock pwl = parentDir.lockForWriting();
		try
		{
			if (spec.getOverwriteStrategy().overwrite(parentDir, name, ETFile.TYPE) == OverwriteResult.CAN_CREATE_NEW_ENTITY)
			{
				long parentDirLastModified = parentDir.getLastModified();
				EFile f = (EFile) parentDir.newEntity(ETFile.TYPE, name, null);
				Lock wl = f.lockForWriting();
				try
				{
					parentDir.setLastModified(parentDirLastModified);

					// Don't need this anymore
					pwl.unlock();
					pwl = null;

					try
					{
						// Copy the file contents
						OutputStream os = f.openForWrite();
						try
						{
							StreamUtil.copyStreams(new DataSourceToInputStreamAdapter(src), os, f.getFileSystem().getBufferSize(), headerData.getFileSize());
						}
						finally
						{
							os.close();
						}
					}
					catch (IOException e)
					{
						throw new WrappedIOException(e);
					}

					f.setLastModified(headerData.getLastModificationTime().getTime());
				}
				finally
				{
					wl.unlock();
				}
			}
		}
		finally
		{
			if (pwl != null)
			{
				pwl.unlock();
			}
		}

		// Skip to the next Tar block boundary
		long remaining = headerData.getFileSize() % TarConstants.BLOCK_SIZE;
		if (remaining > 0)
		{
			src.skipBytes(TarConstants.BLOCK_SIZE - remaining);
		}
	}

	protected void extractSymbolicLink(TarEntryHeaderData headerData, DataSource src, Directory targetRoot, TarExtractSpecification spec)
	{
		if (m_ignoreSymbolicLinks)
		{
			System.err.println("Ignoring symbolic link " + headerData.getLocation() + " -> " + headerData.getLinkName());
			skipToNextBlockBoundary(src, headerData.getFileSize());
		}
		else
		{
			throw new TarFileParseException("Cannot extract symbolic link " + headerData.getLocation() + " -> " + headerData.getLinkName() + ". Not supported. " + this.getClass().getName() + " can be configured to ignore symbolic links");
		}
	}

	public void extract(TarEntryHeaderData headerData, DataSource src, Directory targetRoot, TarExtractSpecification spec) throws WrappedIOException, TarFileParseException
	{
		char typeFlag = headerData.getTypeFlag();
		if (headerData.isDirectory())
		{
			extractDirectory(headerData, src, targetRoot, spec);
		}
		else if ((typeFlag == TarConstants.FILE_TYPE_FLAG) || (typeFlag == TarConstants.ALT_FILE_TYPE_FLAG))
		{
			extractFile(headerData, src, targetRoot, spec);
		}
		else if ((typeFlag == TarConstants.SYMBOLIC_LINK_TYPE_FLAG))
		{
			extractSymbolicLink(headerData, src, targetRoot, spec);
		}
		else
		{
			throw new TarFileParseException("Don't know how to extract Tar entries with the type flag " + typeFlag);
		}
	}
}
