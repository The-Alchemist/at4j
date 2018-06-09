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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import org.at4j.archive.ArchiveDirectoryEntry;
import org.at4j.archive.ArchiveEntry;
import org.at4j.archive.ArchiveFileEntry;
import org.at4j.archive.ArchiveSymbolicLinkEntry;
import org.at4j.support.entityfs.PotentialDirectory;
import org.at4j.tar.TarEntry;
import org.at4j.zip.ZipEntry;
import org.entityfs.Directory;
import org.entityfs.EFile;
import org.entityfs.ETDirectory;
import org.entityfs.ETFile;
import org.entityfs.Entity;
import org.entityfs.ostrat.OverwriteResult;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.StreamUtil;

/**
 * This is the default {@link EntryExtractionStrategy} used by the
 * {@link ArchiveExtractor}. It extracts all entries to the target directory
 * hierarchy.
 * <p>
 * This object does not have any state. The singleton instance {@link #INSTANCE}
 * may be used instead of instantiating it.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class EntryExtractor implements EntryExtractionStrategy
{
	/**
	 * Singleton instance that may be used instead of instantiating this class.
	 */
	public static final EntryExtractor INSTANCE = new EntryExtractor();

	private void setLastModificationTime(ArchiveEntry<?, ?> ae, Entity ent)
	{
		// Only do this for entry types that record the last modification time
		if (ae instanceof TarEntry)
		{
			ent.setLastModified(((TarEntry) ae).getLastModificationTime().getTime());
		}
		else if (ae instanceof ZipEntry)
		{
			ent.setLastModified(((ZipEntry) ae).getLastModified().getTime());
		}
	}
	
	public Directory extractDirectory(ArchiveDirectoryEntry<?, ?> dir, PotentialDirectory target, ExtractSpecification spec)
	{
		Directory parent = target.getDirectory();
		if (parent == null)
		{
			return null;
		}	

		String name = dir.getName();
		Lock wl = parent.lockForWriting();
		try
		{
			switch (spec.getOverwriteStrategy().overwrite(parent, name, ETDirectory.TYPE))
			{
				case CAN_CREATE_NEW_ENTITY:
					return (Directory) parent.newEntity(ETDirectory.TYPE, name, null);
				case KEPT_OLD_DIRECTORY:
					Directory res = (Directory) parent.getEntityOrNull(name);
					// Just to be sure
					res.getClass();
					return res;
				default:
					return null;
			}
		}
		finally
		{
			wl.unlock();
		}
	}

	public void postProcessDirectory(ArchiveDirectoryEntry<?, ?> dir, Directory target, ExtractSpecification spec)
	{
		if (spec.isPreserveModificationTimes())
		{
			setLastModificationTime(dir, target);
		}
	}
	
	public void extractFile(ArchiveFileEntry<?, ?> f, PotentialDirectory target, ExtractSpecification spec)
	{
		Directory parent = target.getDirectory();
		if (parent != null)
		{
			String name = f.getName();
			Lock pwl = parent.lockForWriting();
			try
			{
				if (spec.getOverwriteStrategy().overwrite(parent, name, ETFile.TYPE) == OverwriteResult.CAN_CREATE_NEW_ENTITY)
				{
					EFile ef = (EFile) parent.newEntity(ETFile.TYPE, name, null);
					Lock wl = ef.lockForWriting();
					try
					{
						// Don't need this anymore
						pwl.unlock();
						pwl = null;

						try
						{
							InputStream is = f.openForRead();
							try
							{
								OutputStream os = ef.openForWrite();
								try
								{
									StreamUtil.copyStreams(is, os, ef.getFileSystem().getBufferSize(), f.getDataSize());
								}
								finally
								{
									os.close();
								}
							}
							finally
							{
								is.close();
							}
						}
						catch (IOException e)
						{
							throw new WrappedIOException(e);
						}
						
						if (spec.isPreserveModificationTimes())
						{
							setLastModificationTime(f, ef);
						}
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
		}
	}

	/**
	 * This method just prints a warning to stderr about that it could not
	 * extract the symbolic link.
	 */
	public void extractSymbolicLink(ArchiveSymbolicLinkEntry<?, ?> l, PotentialDirectory target, ExtractSpecification spec)
	{
		System.err.println("Ignoring symbolic link " + l.getLocation() + " -> " + l.getLinkTarget());
	}
}
