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

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.entityfs.DirectoryView;
import org.entityfs.EntityView;
import org.entityfs.NamedReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.exception.EntityNotFoundException;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.filter.Filter;

/**
 * This is a generic interface for an archive builder, i.e. an object used to
 * build a file archive containing files and directories.
 * <p>
 * Files and directories, entries, are added used any of the {@code add}
 * methods. Every entry has a unique {@link org.entityfs.el.AbsoluteLocation} in
 * the archive's directory hierarchy. If an entry is added at a location where
 * an entry already exists, the previous entry is made invisible, but is still
 * present in the archive.
 * <p>
 * The builder does not verify the directory hierarchy for added entries. The
 * programs used to read archives usually cope well with missing parent
 * directories of entries.
 * <p>
 * Every entry also has some metadata associated with it. Exactly what metadata
 * that an entry has depends on the type of archive being built and on the
 * effective {@link ArchiveEntrySettings} for the entry when it is added to the
 * archive. The effective settings for an entry is determined thus:
 * <ol>
 * <li>Start with the default settings for the entry type. There are different
 * default settings for file and directory entries. See
 * {@link #getDefaultFileEntrySettings()} and
 * {@link #getDefaultDirectoryEntrySettings()}.</li>
 * <li>Evaluate all global entry settings rules (
 * {@link ArchiveEntrySettingsRule}):s to see if any rule applies to the entry
 * being added. For all rules that apply, their settings are combined with the
 * default settings using the
 * {@link ArchiveEntrySettings#combineWith(ArchiveEntrySettings)} method. The
 * rules are applied in the order that they occur in the archive builder's rules
 * list.</li>
 * <li>Combine the resulting settings from the previous two steps with any
 * specific settings supplied to the add method when adding the entry to the
 * archive. The supplied settings are combined with the previous settings using
 * the {@link ArchiveEntrySettings#combineWith(ArchiveEntrySettings)} method.</li>
 * </ol>
 * The effect of the algorithm above is that settings from global rules that
 * apply to the entry being added, override the default settings for the entity
 * type, and that specific settings supplied when adding the entry override
 * default settings and settings from the global rules.
 * <p>
 * When the archive builder is created, it opens the supplied file for writing
 * (or for random access). Entries are written to the archive as they are added.
 * When the archive builder is {@link #close()}:d, it finishes writing the
 * archive file and closes it. Most archive builder methods throw an
 * {@link IllegalStateException} if they are called after closing the object.
 * <p>
 * Archive builders are <i>not</i> safe to use concurrently from several threads
 * without external synchronization.
 * <p>
 * If the archive file is in a locking {@link org.entityfs.FileSystem}, the
 * archive builder object acquires a write lock on the file when the object is
 * created. The {@link #close()} method releases the write lock.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <U> The archive builder implementation.
 * @param <V> The type of settings used for the archive builder implementation.
 */
public interface ArchiveBuilder<U extends ArchiveBuilder<U, V>, V extends ArchiveEntrySettings<V>>
{
	/**
	 * Set the default settings for file entries that are added to the archive
	 * from now on.
	 * <p>
	 * The supplied object is combined with the default default file entry
	 * settings object using the
	 * {@link ArchiveEntrySettings#combineWith(ArchiveEntrySettings)} method.
	 * This means that the client does not have to specify values for the
	 * properties where the default values should be used.
	 * <p>
	 * The actual settings that are used for entries that are added to the
	 * archive is determined using the algorithm described in the interface
	 * documentation above.
	 * @param s The new default file entry settings.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #setDefaultDirectoryEntrySettings(ArchiveEntrySettings)
	 */
	U setDefaultFileEntrySettings(V s) throws IllegalStateException;

	/**
	 * Get (a copy of) the default settings for the file entries added to this
	 * archive builder.
	 * <p>
	 * The actual settings that are used for entries that are added to the
	 * archive is determined using the algorithm described in the interface
	 * documentation above.
	 * @return The default settings used for file entries added to the archive
	 * builder.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #getDefaultDirectoryEntrySettings()
	 * @see #setDefaultFileEntrySettings(ArchiveEntrySettings)
	 */
	V getDefaultFileEntrySettings() throws IllegalStateException;

	/**
	 * Set the default settings for directory entries that are added to the
	 * archive from now on.
	 * <p>
	 * The supplied object is combined with the default default directory entry
	 * settings object using the
	 * {@link ArchiveEntrySettings#combineWith(ArchiveEntrySettings)} method.
	 * This means that the client does not have to specify values for the
	 * properties where the default values should be used.
	 * <p>
	 * The actual settings that are used for entries that are added to the
	 * archive is determined using the algorithm described in the interface
	 * documentation above.
	 * @param s The new default directory entry settings.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #setDefaultFileEntrySettings(ArchiveEntrySettings)
	 */
	U setDefaultDirectoryEntrySettings(V s) throws IllegalStateException;

	/**
	 * Get (a copy of) the default settings for the directory entries added to
	 * this archive builder.
	 * <p>
	 * The actual settings that are used for entries that are added to the
	 * archive is determined using the algorithm described in the interface
	 * documentation above.
	 * @return The default settings used for directory entries added to the
	 * archive builder
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #getDefaultFileEntrySettings()
	 * @see #setDefaultDirectoryEntrySettings(ArchiveEntrySettings)
	 */
	V getDefaultDirectoryEntrySettings() throws IllegalStateException;

	/**
	 * Append an entry settings rule to the end of the rule list. This rule will
	 * be used for entries added to the archive after this method has been
	 * called.
	 * <p>
	 * The rule list is used when determining the settings that an entry added
	 * to the archive should have. The actual settings that are used for entries
	 * that are added to the archive is determined using the algorithm described
	 * in the interface documentation above.
	 * @param rule The rule to append to the end of the rule list.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #addRules(Collection)
	 * @see #removeRule(ArchiveEntrySettingsRule)
	 * @see #getRules()
	 */
	U addRule(ArchiveEntrySettingsRule<? extends V> rule) throws IllegalStateException;

	/**
	 * Append a list of entry settings rules to the end of the rule list. The
	 * rules are appended in the order that they are returned when iterating
	 * over the supplied collection.
	 * <p>
	 * The rules will be used for entries added to the archive after this method
	 * has been called.
	 * <p>
	 * The rule list is used when determining the settings that an entry added
	 * to the archive should have. The actual settings that are used for entries
	 * that are added to the archive is determined using the algorithm described
	 * in the interface documentation above.
	 * @param rules The rules to append to the end of the rule list.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #addRule(ArchiveEntrySettingsRule)
	 * @see #removeRule(ArchiveEntrySettingsRule)
	 * @see #getRules()
	 */
	U addRules(Collection<? extends ArchiveEntrySettingsRule<? extends V>> rules) throws IllegalStateException;

	/**
	 * Remove an entry settings rule from the rule list. This rule will not be
	 * used for the entries added to the archive after this method has been
	 * called.
	 * <p>
	 * The rule list is used when determining the settings that an entry added
	 * to the archive should have. The actual settings that are used for entries
	 * that are added to the archive is determined using the algorithm described
	 * in the interface documentation above.
	 * @param rule The rule to remove from the list.
	 * @return {@code true} if a rule was removed from the list, {@code false}
	 * if not. If no rule was removed from the list, that means that the
	 * supplied rule was not present in the list to begin with.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #addRule(ArchiveEntrySettingsRule)
	 * @see #getRules()
	 */
	boolean removeRule(ArchiveEntrySettingsRule<? extends V> rule) throws IllegalStateException;

	/**
	 * Get a copy of the list of rules that are used to determine the entry
	 * settings for entries added to this archive.
	 * <p>
	 * The actual settings that are used for entries that are added to the
	 * archive is determined using the algorithm described in the interface
	 * documentation above.
	 * @return A copy of the list of rules that are used to determine the entry
	 * settings for entries added to this archive.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @see #addRule(ArchiveEntrySettingsRule)
	 * @see #removeRule(ArchiveEntrySettingsRule)
	 */
	List<ArchiveEntrySettingsRule<? extends V>> getRules() throws IllegalStateException;

	/**
	 * Add a file entry to the archive's root directory. The file is added with
	 * the default file settings, combined with settings from the global rules
	 * that apply to the file, if any.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is
	 * temporarily locked for reading by this method.
	 * <p>
	 * The name of the added file entry will be the name of the added file. If
	 * you want another name, use a
	 * {@link org.entityfs.util.NamedReadableFileAdapter}.
	 * @param f The file to add to the archive.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(NamedReadableFile f) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add a file entry to the specified parent directory in the archive. The
	 * file is added with the default file settings, combined with settings from
	 * the global rules that apply to the file, if any.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is
	 * temporarily locked for reading by this method.
	 * <p>
	 * The name of the added file entry will be the name of the added file. If
	 * you want another name, use a
	 * {@link org.entityfs.util.NamedReadableFileAdapter}.
	 * @param f The file to add to the archive.
	 * @param parentDirLocation The location of the parent directory. It is
	 * <i>not</i> required that this directory is present in the archive.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(NamedReadableFile f, AbsoluteLocation parentDirLocation) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add a file entry to the specified parent directory in the archive. The
	 * supplied settings is combined with the default file settings and the
	 * settings from the global rules that apply to the file, if any.
	 * <p>
	 * If the file is in a locking {@link org.entityfs.FileSystem}, it is
	 * temporarily locked for reading by this method.
	 * <p>
	 * The name of the added file entry will be the name of the added file. If
	 * you want another name, use a
	 * {@link org.entityfs.util.NamedReadableFileAdapter}.
	 * @param f The file to add to the archive.
	 * @param parentDirLocation The location of the parent directory. It is
	 * <i>not</i> required that this directory is present in the archive.
	 * @param settings The custom settings to use for this file. The custom
	 * settings are combined with the default settings and global rule settings
	 * as described above. If this is set to {@code null}, only the default file
	 * settings combined with settings from the global rules that apply to this
	 * file are used.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the file cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(NamedReadableFile f, AbsoluteLocation parentDirLocation, V settings) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add a directory entry to the archive's root directory. The directory is
	 * added with the default directory settings, combined with settings from
	 * the global rules that apply to the directory, if any.
	 * <p>
	 * If the directory is in a locking {@link org.entityfs.FileSystem}, it is
	 * temporarily locked for reading by this method.
	 * <p>
	 * The name of the directory entry will be the name of the added directory.
	 * @param d The directory to add to the archive.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the directory cannot be added, for
	 * some reason other than an I/O error.
	 */
	U add(DirectoryView d) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add the directory entry to the specified parent directory in the archive.
	 * The directory is added with the default directory settings, combined with
	 * settings from the global rules that apply to the directory, if any.
	 * <p>
	 * If the directory is in a locking {@link org.entityfs.FileSystem}, it is
	 * temporarily locked for reading by this method.
	 * <p>
	 * The name of the directory entry will be the name of the added directory.
	 * @param d The directory to add to the archive.
	 * @param parentDirLocation The location of the parent directory. It is
	 * <i>not</i> required that this directory is present in the archive.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the directory cannot be added, for
	 * some reason other than an I/O error.
	 */
	U add(DirectoryView d, AbsoluteLocation parentDirLocation) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add a directory entry to the specified parent directory in the archive.
	 * The supplied settings is combined with the default directory settings and
	 * the settings from the global rules that apply to the directory, if any.
	 * <p>
	 * If the directory is in a locking {@link org.entityfs.FileSystem}, it is
	 * temporarily locked for reading by this method.
	 * <p>
	 * The name of the directory entry will be the name of the added directory.
	 * @param d The directory to add to the archive.
	 * @param parentDirLocation The location of the parent directory. It is
	 * <i>not</i> required that this directory is present in the archive.
	 * @param settings The custom settings to use for this directory. The custom
	 * settings are combined with the default settings and global rule settings
	 * as described above. If this is set to {@code null}, only the default
	 * directory settings combined with settings from the global rules that
	 * apply to this directory are used.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the directory cannot be added, for
	 * some reason other than an I/O error.
	 */
	U add(DirectoryView d, AbsoluteLocation parentDirLocation, V settings) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add a file or directory entry to the archive's root directory. The entry
	 * is added with the default settings for its entity type (file or
	 * directory), combined with settings from the global rules that apply to
	 * the entry, if any.
	 * <p>
	 * The name of the file entry will be the name of the added file. If you
	 * want another name for the entry, wrap the file in a
	 * {@link org.entityfs.util.FileReadableFile} and then in a
	 * {@link org.entityfs.util.NamedReadableFileAdapter}.
	 * @param f The file or directory to add to the archive.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the {@code File} object does not
	 * reference an existing file or directory.
	 * @throws ArchiveEntryAddException If the entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(File f) throws IllegalStateException, WrappedIOException, EntityNotFoundException, ArchiveEntryAddException;

	/**
	 * Add a file or directory entry to the specified parent directory in the
	 * archive. The entry is added with the default settings for its entity type
	 * (file or directory), combined with settings from the global rules that
	 * apply to the entry, if any.
	 * <p>
	 * The name of the file entry will be the name of the added file. If you
	 * want another name for the entry, wrap the file in a
	 * {@link org.entityfs.util.FileReadableFile} and then in a
	 * {@link org.entityfs.util.NamedReadableFileAdapter}.
	 * @param f The file or directory to add to the archive.
	 * @param parentDirLocation The location of the parent directory. It is
	 * <i>not</i> required that this directory is present in the archive.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the {@code File} object does not
	 * reference an existing file or directory.
	 * @throws ArchiveEntryAddException If the entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U add(File f, AbsoluteLocation parentDirLocation) throws IllegalStateException, WrappedIOException, EntityNotFoundException, ArchiveEntryAddException;

	/**
	 * Add a file or directory entry to the specified parent directory in the
	 * archive. The supplied settings is combined with the default settings for
	 * its entity type (file or directory) and the settings from the global
	 * rules that apply to the entry, if any.
	 * <p>
	 * The name of the file entry will be the name of the added file. If you
	 * want another name for the entry, wrap the file in a
	 * {@link org.entityfs.util.FileReadableFile} and then in a
	 * {@link org.entityfs.util.NamedReadableFileAdapter}.
	 * @param f The file or directory to add to the archive.
	 * @param parentDirLocation The location of the parent directory. It is
	 * <i>not</i> required that this directory is present in the archive.
	 * @param settings The custom settings to use for this entry. The custom
	 * settings are combined with the default settings and global rule settings
	 * as described above. If this is set to {@code null}, only the default
	 * settings combined with settings from the global rules that apply to this
	 * entry are used.
	 * @return {@code this}
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the {@code File} object does not
	 * reference an existing file or directory.
	 * @throws ArchiveEntryAddException If the directory cannot be added, for
	 * some reason other than an I/O error.
	 */
	U add(File f, AbsoluteLocation parentDirLocation, V settings) throws IllegalStateException, WrappedIOException, EntityNotFoundException, ArchiveEntryAddException;

	/**
	 * Add the supplied directory recursively to the specified position in the
	 * archive. Each entry is added with the default settings for its entity
	 * type (file or directory), combined with settings from the global rules
	 * that apply to the entry, if any.
	 * <p>
	 * If the added directory is in a locking {@link org.entityfs.FileSystem},
	 * all added entities are temporarily locked by this method as they are
	 * being added to the archive.
	 * <p>
	 * If an error occurs when adding an entry, the recursive add operation
	 * terminates and the rest of the entries that were to be added are ignored.
	 * The archive file will then contain all entries that had been added before
	 * the error occurred.
	 * @param d The directory to add recursively to the archive.
	 * @param rootDirLocation The location where to put the root of the added
	 * directory tree. If this is the root directory ({@code /}), the contents
	 * of {@code d} are added directly to the archive's root directory.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If an entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U addRecursively(DirectoryView d, AbsoluteLocation rootDirLocation) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add the supplied directory recursively to the specified position in the
	 * archive. An entity is only added if it passes the supplied filter. Each
	 * added entry is added with the default settings for its entity type (file
	 * or directory), combined with settings from the global rules that apply to
	 * the entry, if any.
	 * <p>
	 * If the added directory is in a locking {@link org.entityfs.FileSystem},
	 * all added entities are temporarily locked by this method as they are
	 * being added to the archive.
	 * <p>
	 * If an error occurs when adding an entry, the recursive add operation
	 * terminates and the rest of the entries that were to be added are ignored.
	 * The archive file will then contain all entries that had been added before
	 * the error occurred.
	 * @param d The directory to add recursively to the archive.
	 * @param rootDirLocation The location where to put the root of the added
	 * directory tree. If this is the root directory ({@code /}), the contents
	 * of {@code d} are added directly to the archive's root directory.
	 * @param filter The filter that is used to decide if an entity is to be
	 * added to the archive or not. This filter does <i>not</i> affect how the
	 * method traverses the directory tree in any way.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If an entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U addRecursively(DirectoryView d, AbsoluteLocation rootDirLocation, Filter<? super EntityView> filter) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add the supplied directory recursively to the specified position in the
	 * archive. An entity is only added if it passes the supplied filter. Each
	 * added entry is added with the default settings for its entity type (file
	 * or directory), combined with settings from the global rules that apply to
	 * the entry, combined with the settings from the supplied rules that apply
	 * to the entry.
	 * <p>
	 * If the added directory is in a locking {@link org.entityfs.FileSystem},
	 * all added entities are temporarily locked by this method as they are
	 * being added to the archive.
	 * <p>
	 * If an error occurs when adding an entry, the recursive add operation
	 * terminates and the rest of the entries that were to be added are ignored.
	 * The archive file will then contain all entries that had been added before
	 * the error occurred.
	 * @param d The directory to add recursively to the archive.
	 * @param rootDirLocation The location where to put the root of the added
	 * directory tree. If this is the root directory ({@code /}), the contents
	 * of {@code d} are added directly to the archive's root directory.
	 * @param filter The filter that is used to decide if an entity is to be
	 * added to the archive or not. This filter does <i>not</i> affect how the
	 * method traverses the directory tree in any way.
	 * @param rules Settings rules for this add operation.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If an entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U addRecursively(DirectoryView d, AbsoluteLocation rootDirLocation, Filter<? super EntityView> filter, ArchiveEntrySettingsRule<? extends V>... rules) throws IllegalStateException, WrappedIOException, ArchiveEntryAddException;

	/**
	 * Add the supplied file or directory recursively to the specified position
	 * in the archive. Each entry is added with the default settings for its
	 * entity type (file or directory), combined with settings from the global
	 * rules that apply to the entry, if any.
	 * <p>
	 * If an error occurs when adding an entry, the recursive add operation
	 * terminates and the rest of the entries that were to be added are ignored.
	 * The archive file will then contain all entries that had been added before
	 * the error occurred.
	 * @param f The file or directory to add recursively to the archive.
	 * @param rootDirLocation The location where to put the root of the added
	 * directory tree. If this is the root directory ({@code /}), the contents
	 * of {@code f} (if it is a directory) are added directly to the archive's
	 * root directory.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the {@code File} object does not
	 * reference an existing file or directory.
	 * @throws ArchiveEntryAddException If an entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U addRecursively(File f, AbsoluteLocation rootDirLocation) throws IllegalStateException, WrappedIOException, EntityNotFoundException, ArchiveEntryAddException;

	/**
	 * Add the supplied file or directory recursively to the specified position
	 * in the archive. Each entry is added with the default settings for its
	 * entity type (file or directory), combined with settings from the global
	 * rules that apply to the entry, combined with settings from the supplied
	 * rules that apply to the entry.
	 * <p>
	 * If an error occurs when adding an entry, the recursive add operation
	 * terminates and the rest of the entries that were to be added are ignored.
	 * The archive file will then contain all entries that had been added before
	 * the error occurred.
	 * @param f The file or directory to add recursively to the archive.
	 * @param rootDirLocation The location where to put the root of the added
	 * directory tree. If this is the root directory ({@code /}), the contents
	 * of {@code f} (if it is a directory) are added directly to the archive's
	 * root directory.
	 * @param rules Settings rules for this add operation.
	 * @return {@code this}.
	 * @throws IllegalStateException If the archive builder is closed.
	 * @throws WrappedIOException On I/O errors.
	 * @throws EntityNotFoundException If the {@code File} object does not
	 * reference an existing file or directory.
	 * @throws ArchiveEntryAddException If an entry cannot be added, for some
	 * reason other than an I/O error.
	 */
	U addRecursively(File f, AbsoluteLocation rootDirLocation, ArchiveEntrySettingsRule<? extends V>... rules) throws IllegalStateException, WrappedIOException, EntityNotFoundException, ArchiveEntryAddException;

	/**
	 * Is this archive builder closed?
	 * @return {@code true} if this archive builder is closed.
	 */
	boolean isClosed();

	/**
	 * Close this archive builder, finish the archive and release all of the
	 * resources associated with the archive builder. This releases the write
	 * lock on the archive file (if it is in a locking
	 * {@link org.entityfs.FileSystem}).
	 * <p>
	 * It is safe to call this method several times on the same archive builder.
	 */
	void close();
}
