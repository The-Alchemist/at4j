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

import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This interface extends the {@link ArchiveBuilder} interface with the
 * capability to add data from {@link java.io.InputStream}:s to the archive.
 * <p>
 * Since the size of data in a stream is not known beforehand, this requires
 * that the archive builder has random access to the file it builds since it has
 * to rewind it to write a file entry's size after adding the entry itself. This
 * makes it impossible to write the archive to a stream.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <U> The archive builder implementation.
 * @param <V> The type of settings used for the archive builder implementation.
 */
public interface StreamAddCapableArchiveBuilder<U extends StreamAddCapableArchiveBuilder<U, V>, V extends ArchiveEntrySettings<V>> extends ArchiveBuilder<U, V>
{
	/**
	 * Add a file entry containing all data that can be read from the supplied
	 * stream. It is put at the specified location in the archive. The file is
	 * added with the default file settings, combined with settings from the
	 * global rules that apply to the file, if any.
	 * @param is The stream to read file data from. The stream is <i>not</i>
	 * closed by this method.
	 * @param entityLocation The location of the file entry in the archive.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(InputStream is, AbsoluteLocation entityLocation) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add a file entry containing all data that can be read from the supplied
	 * stream. It is put at the specified location in the archive. The supplied
	 * settings is combined with the default file settings and the settings from
	 * the global rules that apply to the entry, if any.
	 * @param is The stream to read file data from. The stream is <i>not</i>
	 * closed by this method.
	 * @param entityLocation The location of the file entry in the archive.
	 * @param settings The custom settings to use for this entry. The custom
	 * settings are combined with the default settings and global rule settings
	 * as described above. If this is set to {@code null}, only the default
	 * settings combined with settings from the global rules that apply to this
	 * file are used.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(InputStream is, AbsoluteLocation entityLocation, V settings) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;
}
