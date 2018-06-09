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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.entityfs.DirectoryView;
import org.entityfs.EFile;
import org.entityfs.ETDirectory;
import org.entityfs.ETFile;
import org.entityfs.EntityView;
import org.entityfs.FileSystem;
import org.entityfs.NamedReadableFile;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.exception.EntityNotFoundException;
import org.entityfs.fs.FSROFileSystemBuilder;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.filter.Filter;
import org.entityfs.util.io.ReadWritableFileAdapter;

/**
 * This is an abstract base implementation of an archive builder that implements
 * most of the methods in the {@link ArchiveBuilder} interface. It can be
 * extended by objects that build specific types of archives.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ArchiveBuilder
 * @param <U> The concrete type of this builder.
 * @param <V> The type of settings object that this builder uses.
 */
public abstract class AbstractArchiveBuilder<U extends AbstractArchiveBuilder<U, V>, V extends ArchiveEntrySettings<V>> implements ArchiveBuilder<U, V>
{
	private static final ArchiveEntrySettingsRule<?>[] NO_RULES = new ArchiveEntrySettingsRule[0];

	private V m_defaultFileEntrySettings;
	private V m_defaultDirectoryEntrySettings;
	private final List<ArchiveEntrySettingsRule<? extends V>> m_rules = new ArrayList<ArchiveEntrySettingsRule<? extends V>>(2);

	/**
	 * Create a new archive builder.
	 * @param defaultFileEntrySettings Default settings for file entries.
	 * @param defaultDirectoryEntrySettings Default settings for directory
	 * entries.
	 */
	protected AbstractArchiveBuilder(V defaultFileEntrySettings, V defaultDirectoryEntrySettings)
	{
		// Null checks
		defaultFileEntrySettings.getClass();
		defaultDirectoryEntrySettings.getClass();

		m_defaultFileEntrySettings = defaultFileEntrySettings;
		m_defaultDirectoryEntrySettings = defaultDirectoryEntrySettings;
	}

	/**
	 * Subclasses implement this method to throw an
	 * {@link IllegalStateException} if it is called after the archive builder
	 * has been closed.
	 * @throws IllegalStateException If the archive builder has been closed.
	 */
	protected abstract void assertNotClosed() throws IllegalStateException;

	/**
	 * Subclasses implement this method to return a default settings object for
	 * file entries if the client does not specify one when creating the archive
	 * builder object.
	 * @return The default settings object for file entries if none is specified
	 * by the client.
	 */
	protected abstract V getDefaultDefaultFileEntrySettings();

	/**
	 * Subclasses implement this method to return a default settings object for
	 * directory entries if the client does not specify one when creating the
	 * archive builder object.
	 * @return The default settings object for directory entries if none is
	 * specified by the client.
	 */
	protected abstract V getDefaultDefaultDirectoryEntrySettings();

	/**
	 * Get this object cast to its actual type.
	 * @return {@code this}.
	 */
	@SuppressWarnings("unchecked")
	protected U getThis()
	{
		return (U) this;
	}

	public U setDefaultFileEntrySettings(V s) throws IllegalStateException
	{
		assertNotClosed();
		// Null check
		s.getClass();
		m_defaultFileEntrySettings = getDefaultDefaultFileEntrySettings().combineWith(s);
		return getThis();
	}

	public V getDefaultFileEntrySettings() throws IllegalStateException
	{
		assertNotClosed();
		// Return a defensive copy
		return m_defaultFileEntrySettings.clone();
	}

	public U setDefaultDirectoryEntrySettings(V s) throws IllegalStateException
	{
		assertNotClosed();
		// Null check
		s.getClass();
		m_defaultDirectoryEntrySettings = getDefaultDefaultDirectoryEntrySettings().combineWith(s);
		return getThis();
	}

	public V getDefaultDirectoryEntrySettings() throws IllegalStateException
	{
		assertNotClosed();
		// Return a defensive copy
		return m_defaultDirectoryEntrySettings.clone();
	}

	public U addRule(ArchiveEntrySettingsRule<? extends V> rule) throws IllegalStateException
	{
		assertNotClosed();
		// Null check
		m_rules.add(rule);
		return getThis();
	}

	public U addRules(Collection<? extends ArchiveEntrySettingsRule<? extends V>> rules) throws IllegalStateException
	{
		assertNotClosed();

		// Null checks
		for (ArchiveEntrySettingsRule<? extends V> rule : rules)
		{
			rule.getClass();
		}

		m_rules.addAll(rules);
		return getThis();
	}

	public boolean removeRule(ArchiveEntrySettingsRule<? extends V> rule) throws IllegalStateException
	{
		assertNotClosed();
		return m_rules.remove(rule);
	}

	public List<ArchiveEntrySettingsRule<? extends V>> getRules() throws IllegalStateException
	{
		assertNotClosed();
		return new ArrayList<ArchiveEntrySettingsRule<? extends V>>(m_rules);
	}

	/**
	 * Get the effective settings for a file entry that is about to be added to
	 * the archive.
	 * <p>
	 * The effective settings are created by combining first the default file
	 * entry settings with all global rules that apply to the file entry, and
	 * then by combining the resulting settings with the supplied settings
	 * object (if the supplied object is not {@code null}).
	 * @param entity The file entity to add to the archive. This may be a
	 * {@link org.entityfs.ReadableFile} or an {@link java.io.InputStream}.
	 * @param location The absolute location where the entry should be put in
	 * the archive.
	 * @param entrySettings The settings that the default settings and settings
	 * from global rules should be combined with. This may be {@code null}.
	 * @return The effective settings.
	 */
	protected V getEffectiveSettingsForFile(Object entity, AbsoluteLocation location, V entrySettings)
	{
		V res = m_defaultFileEntrySettings;
		EntryToArchive etar = new EntryToArchive(location, ETFile.TYPE, entity);
		for (ArchiveEntrySettingsRule<? extends V> rule : m_rules)
		{
			if (rule.getFilter().matches(etar))
			{
				res = res.combineWith(rule.getSettings());
			}
		}
		return entrySettings != null ? res.combineWith(entrySettings) : res;
	}

	/**
	 * Get the effective settings for a file entry that is about to be added to
	 * the archive.
	 * <p>
	 * The effective settings are created by combining first the default file
	 * entry settings with all global rules that apply to the file entry, and
	 * then by combining the resulting settings with the settings objects for
	 * all of the supplied rules that apply to the file entry.
	 * @param entity The file entity to add to the archive. This may be a
	 * {@link org.entityfs.ReadableFile} or an {@link java.io.InputStream}.
	 * @param location The absolute location where the entry should be put in
	 * the archive.
	 * @param rules The rules to evaluate for the file entry. This may be
	 * {@code null}.
	 * @return The effective settings
	 */
	protected V getEffectiveSettingsForFile(Object entity, AbsoluteLocation location, ArchiveEntrySettingsRule<? extends V>[] rules)
	{
		V res = m_defaultFileEntrySettings;
		EntryToArchive etar = new EntryToArchive(location, ETFile.TYPE, entity);
		for (ArchiveEntrySettingsRule<? extends V> rule : m_rules)
		{
			if (rule.getFilter().matches(etar))
			{
				res = res.combineWith(rule.getSettings());
			}
		}
		if (rules != null)
		{
			for (ArchiveEntrySettingsRule<? extends V> rule : rules)
			{
				if (rule.getFilter().matches(etar))
				{
					res = res.combineWith(rule.getSettings());
				}
			}
		}
		return res;
	}

	/**
	 * Get the effective settings for a directory entry that is about to be
	 * added to the archive.
	 * <p>
	 * The effective settings are created by combining first the default
	 * directory entry settings with all global rules that apply to the
	 * directory entry, and then by combining the resulting settings with the
	 * supplied settings object (if the supplied object is not {@code null}).
	 * @param entity The directory entity to add to the archive. This may be a
	 * {@link org.entityfs.DirectoryView} or a {@link java.io.File}.
	 * @param location The absolute location where the entry should be put in
	 * the archive.
	 * @param entrySettings The settings that the default settings and settings
	 * from global rules should be combined with. This may be {@code null}.
	 * @return The effective settings.
	 */
	protected V getEffectiveSettingsForDirectory(Object entity, AbsoluteLocation location, V entrySettings)
	{
		V res = m_defaultDirectoryEntrySettings;
		EntryToArchive etar = new EntryToArchive(location, ETDirectory.TYPE, entity);
		for (ArchiveEntrySettingsRule<? extends V> rule : m_rules)
		{
			if (rule.getFilter().matches(etar))
			{
				res = res.combineWith(rule.getSettings());
			}
		}
		return entrySettings != null ? res.combineWith(entrySettings) : res;
	}

	/**
	 * Get the effective settings for a directory entry that is about to be
	 * added to the archive.
	 * <p>
	 * The effective settings are created by combining first the default
	 * directory entry settings with all global rules that apply to the
	 * directory entry, and then by combining the resulting settings with the
	 * settings objects for all of the supplied rules that apply to the
	 * directory entry.
	 * @param entity The directory entity to add to the archive. This may be a
	 * {@link org.entityfs.DirectoryView} or a {@link java.io.File}.
	 * @param location The absolute location where the entry should be put in
	 * the archive.
	 * @param rules The rules to evaluate for the directory entry. This may be
	 * {@code null}.
	 * @return The effective settings
	 */
	protected V getEffectiveSettingsForDirectory(Object entity, AbsoluteLocation location, ArchiveEntrySettingsRule<? extends V>[] rules)
	{
		V res = m_defaultDirectoryEntrySettings;
		EntryToArchive etar = new EntryToArchive(location, ETDirectory.TYPE, entity);
		for (ArchiveEntrySettingsRule<? extends V> rule : m_rules)
		{
			if (rule.getFilter().matches(etar))
			{
				res = res.combineWith(rule.getSettings());
			}
		}
		if (rules != null)
		{
			for (ArchiveEntrySettingsRule<? extends V> rule : rules)
			{
				if (rule.getFilter().matches(etar))
				{
					res = res.combineWith(rule.getSettings());
				}
			}
		}
		return res;
	}

	/**
	 * This callback method is implemented by subclasses to add a directory
	 * entry to the archive. The entry is added at the end of the archive file.
	 * <p>
	 * <b>Note:</b> If this method throws an exception, it is the method's
	 * responsibility to make sure that the archive file is left in a consistent
	 * state. This should probably always mean that the failed entry is
	 * truncated from the archive file and that the current archive file pointer
	 * is restored to where it was before the method was called.
	 * @param location The location of the entry in the archive.
	 * @param d The directory. This may be a {@link org.entityfs.DirectoryView}
	 * or a {@link java.io.File}.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the directory was last modified.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the entry could not be added to the
	 * archive for some other reason than an I/O error.
	 */
	protected abstract void addDirectoryCallback(AbsoluteLocation location, Object d, V effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException;

	/**
	 * This callback method is implemented by subclasses to add a file entry to
	 * the archive. The entry is added at the end of the archive file.
	 * <p>
	 * <b>Note:</b> If this method throws an exception, it is the method's
	 * responsibility to make sure that the archive file is left in a consistent
	 * state. This should probably always mean that the failed entry is
	 * truncated from the archive file and that the current archive file pointer
	 * is restored to where it was before the method was called.
	 * @param location The location of the entry in the archive.
	 * @param f The file.
	 * @param effectiveSettings The effective settings for the entry.
	 * @param lastModified The time when the file was last modified.
	 * @throws WrappedIOException On I/O errors.
	 * @throws ArchiveEntryAddException If the entry could not be added to the
	 * archive for some other reason than an I/O error.
	 */
	protected abstract void addFileCallback(AbsoluteLocation location, ReadableFile f, V effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException;

	public U add(NamedReadableFile f) throws IllegalStateException, WrappedIOException
	{
		return add(f, AbsoluteLocation.ROOT_DIR, null);
	}

	public U add(NamedReadableFile f, AbsoluteLocation parentDirLocation) throws IllegalStateException, WrappedIOException
	{
		return add(f, parentDirLocation, null);
	}

	public U add(NamedReadableFile f, AbsoluteLocation parentDirLocation, V settings) throws IllegalStateException, WrappedIOException
	{
		assertNotClosed();
		Lock rl = f.lockForReading();
		try
		{
			AbsoluteLocation entityLocation = parentDirLocation.getChildLocation(f.getName());
			V effectiveSettings = getEffectiveSettingsForFile(f, entityLocation, settings);
			Date lastModified = (f instanceof EntityView) ? new Date(((EntityView) f).getLastModified()) : new Date();
			addFileCallback(entityLocation, f, effectiveSettings, lastModified);
		}
		finally
		{
			rl.unlock();
		}
		return getThis();
	}

	public U add(DirectoryView d) throws IllegalStateException, WrappedIOException
	{
		return add(d, AbsoluteLocation.ROOT_DIR, null);
	}

	public U add(DirectoryView d, AbsoluteLocation parentDirLocation) throws IllegalStateException, WrappedIOException
	{
		return add(d, parentDirLocation, null);
	}

	public U add(DirectoryView d, AbsoluteLocation parentDirLocation, V settings) throws IllegalStateException, WrappedIOException
	{
		assertNotClosed();
		Lock rl = d.lockForReading();
		try
		{
			AbsoluteLocation entityLocation = parentDirLocation.getChildLocation(d.getName());
			V effectiveSettings = getEffectiveSettingsForDirectory(d, entityLocation, settings);
			addDirectoryCallback(entityLocation, d, effectiveSettings, new Date(d.getLastModified()));
		}
		finally
		{
			rl.unlock();
		}
		return getThis();
	}

	public U add(File f) throws IllegalStateException, WrappedIOException, EntityNotFoundException
	{
		return add(f, AbsoluteLocation.ROOT_DIR, null);
	}

	public U add(File f, AbsoluteLocation parentDirLocation) throws IllegalStateException, WrappedIOException, EntityNotFoundException
	{
		return add(f, parentDirLocation, null);
	}

	public U add(File f, AbsoluteLocation parentDirLocation, V settings) throws IllegalStateException, WrappedIOException, EntityNotFoundException
	{
		assertNotClosed();
		if (!f.exists())
		{
			throw new EntityNotFoundException(f + " not found");
		}
		else if (f.isFile())
		{
			add(new ReadWritableFileAdapter(f), parentDirLocation, settings);
		}
		else if (f.isDirectory())
		{
			AbsoluteLocation entityLocation = parentDirLocation.getChildLocation(f.getName());
			V effectiveSettings = getEffectiveSettingsForDirectory(f, entityLocation, settings);
			addDirectoryCallback(entityLocation, f, effectiveSettings, new Date(f.lastModified()));
		}
		else
		{
			throw new RuntimeException("Unknown file type " + f);
		}
		return getThis();
	}

	@SuppressWarnings("unchecked")
	public U addRecursively(DirectoryView d, AbsoluteLocation rootDirLocation) throws IllegalStateException, WrappedIOException
	{
		return addRecursively(d, rootDirLocation, null, (ArchiveEntrySettingsRule<V>[]) NO_RULES);
	}

	@SuppressWarnings("unchecked")
	public U addRecursively(DirectoryView d, AbsoluteLocation rootDirLocation, Filter<? super EntityView> filter) throws IllegalStateException, WrappedIOException
	{
		return addRecursively(d, rootDirLocation, filter, (ArchiveEntrySettingsRule<V>[]) NO_RULES);
	}

	@SuppressWarnings("unchecked")
	public U addRecursively(File f, AbsoluteLocation rootDirLocation) throws IllegalStateException, WrappedIOException, EntityNotFoundException
	{
		return addRecursively(f, rootDirLocation, (ArchiveEntrySettingsRule<V>[]) NO_RULES);
	}

	/**
	 * @param rl A locked read lock for the directory. It is guaranteed to be
	 * unlocked by this method.
	 */
	private void addRecursivelyInternal(DirectoryView d, AbsoluteLocation location, Filter<? super EntityView> filter, ArchiveEntrySettingsRule<? extends V>[] rules, Lock rl) throws WrappedIOException, ArchiveEntryAddException
	{
		Collection<EntityView> childEntities;

		try
		{
			childEntities = d.listEntities();

			// Add the directory if it matches the filter and if the location is
			// not the root dir location.
			if ((filter == null || filter.matches(d)) && (!location.isRootDir()))
			{
				// Add this directory
				V effectiveSettings = getEffectiveSettingsForDirectory(d, location, rules);
				addDirectoryCallback(location, d, effectiveSettings, new Date(d.getLastModified()));
			}
		}
		finally
		{
			rl.unlock();
		}

		// Add child entities
		for (EntityView ev : childEntities)
		{
			if (ev.getType() == ETDirectory.TYPE)
			{
				Lock crl = ev.lockForReading();
				addRecursivelyInternal((DirectoryView) ev, location.getChildLocation(ev.getName()), filter, rules, crl);
			}
			else if (ev.getType() == ETFile.TYPE)
			{
				EFile f = (EFile) ev;
				Lock crl = f.lockForReading();
				try
				{
					if (filter == null || filter.matches(f))
					{
						AbsoluteLocation entityLocation = location.getChildLocation(f.getName());
						V effectiveSettings = getEffectiveSettingsForFile(f, entityLocation, rules);
						addFileCallback(entityLocation, f, effectiveSettings, new Date(f.getLastModified()));
					}
				}
				finally
				{
					crl.unlock();
				}
			}
			else
			{
				throw new ArchiveEntryAddException("Don't know how to add the entry " + ev + " to the archive. Unsupported entity type " + ev.getType());
			}
		}
	}

	public U addRecursively(DirectoryView d, AbsoluteLocation rootDirLocation, Filter<? super EntityView> filter, ArchiveEntrySettingsRule<? extends V>... rules) throws IllegalStateException, WrappedIOException
	{
		assertNotClosed();
		addRecursivelyInternal(d, rootDirLocation, filter, rules, d.lockForReading());
		return getThis();
	}

	public U addRecursively(File f, AbsoluteLocation rootDirLocation, ArchiveEntrySettingsRule<? extends V>... rules) throws IllegalStateException, WrappedIOException, EntityNotFoundException
	{
		assertNotClosed();

		if (!f.exists())
		{
			throw new EntityNotFoundException(f + " not found");
		}
		else if (f.isFile())
		{
			AbsoluteLocation entityLocation = rootDirLocation.getChildLocation(f.getName());
			V effectiveSettings = getEffectiveSettingsForFile(f, entityLocation, rules);
			addFileCallback(entityLocation, new ReadWritableFileAdapter(f), effectiveSettings, new Date(f.lastModified()));
		}
		else if (f.isDirectory())
		{
			// Create a temporary file system
			FileSystem fs = new FSROFileSystemBuilder().setRoot(f).disableEntityValidityControls().create();
			try
			{
				addRecursively(fs.getRootDirectory(), rootDirLocation, null, rules);
			}
			finally
			{
				fs.close();
			}
		}
		else
		{
			throw new RuntimeException("Unknown file type " + f);
		}
		return getThis();
	}
}
