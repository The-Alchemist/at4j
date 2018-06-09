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
package org.at4j.archive.builder;

import java.io.InputStream;
import java.util.Date;

import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This abstract class extends the {@link AbstractArchiveBuilder} with the
 * capability to
 * @author Karl Gustafsson
 * @since 1.0
 * @param <U> The concrete type of this builder.
 * @param <V> The type of settings object that this builder uses.
 */
public abstract class AbstractStreamAddCapableArchiveBuilder<U extends AbstractStreamAddCapableArchiveBuilder<U, V>, V extends ArchiveEntrySettings<V>> extends AbstractArchiveBuilder<U, V> implements StreamAddCapableArchiveBuilder<U, V>
{
	/**
	 * Create a new archive builder.
	 * @param defaultFileEntrySettings Default settings for file entries.
	 * @param defaultDirectoryEntrySettings Default settings for directory
	 * entries.
	 */
	protected AbstractStreamAddCapableArchiveBuilder(V defaultFileEntrySettings, V defaultDirectoryEntrySettings)
	{
		super(defaultFileEntrySettings, defaultDirectoryEntrySettings);
	}

	/**
	 * This callback method is implemented by subclasses to add a file entry
	 * containing data read from a stream to the archive. The entry is added at
	 * the end of the archive file.
	 * <p>
	 * <b>Note:</b> If this method throws an exception, it is the method's
	 * responsibility to make sure that the archive file is left in a consistent
	 * state. This should probably always mean that the failed entry is
	 * truncated from the archive file and that the current archive file pointer
	 * is restored to where it was before the method was called.
	 * @param location The location of the entry in the archive.
	 * @param is The stream to read file data from. The method should read up to
	 * the end of the stream, but not close the stream.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the file was last modified.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the entry could not be added to the
	 * archive for some other reason than an I/O error.
	 */
	protected abstract void addStreamCallback(AbsoluteLocation location, InputStream is, V effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException;

	public U add(InputStream is, AbsoluteLocation entityLocation, V settings) throws IllegalStateException, WrappedIOException
	{
		assertNotClosed();
		V effectiveSettings = getEffectiveSettingsForFile(is, entityLocation, settings);
		addStreamCallback(entityLocation, is, effectiveSettings, new Date());
		return getThis();
	}

	public U add(InputStream is, AbsoluteLocation entityLocation) throws IllegalStateException, WrappedIOException
	{
		return add(is, entityLocation, null);
	}
}
