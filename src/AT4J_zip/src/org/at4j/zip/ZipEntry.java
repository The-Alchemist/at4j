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
package org.at4j.zip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.at4j.archive.AbstractArchiveEntry;
import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ef.ZipEntryExtraField;
import org.at4j.zip.extattrs.MsDosExternalFileAttributes;
import org.at4j.zip.extattrs.ZipExternalFileAttributes;
import org.entityfs.el.AbsoluteLocation;

/**
 * This is an abstract base class for Zip entries. It contains the metadata that
 * is present for all Zip entries, regardless of their type.
 * <p>
 * Zip entries contains the following information:
 * <table>
 * <tr>
 * <th>Property</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Version used to create</td>
 * <td>The equivalent version of the PK-Zip software that was used to create the
 * entry.</td>
 * </tr>
 * <tr>
 * <td>Version needed to extract</td>
 * <td>The equivalent version of the PK-Zip software that is needed to extract
 * the entry.</td>
 * </tr>
 * <tr>
 * <td>Last modification time</td>
 * <td>The last modification time for the file system entity (file, directory)
 * that was used to create the Zip entry.</td>
 * </tr>
 * <tr>
 * <td>{@link ZipGeneralPurposeBitFlags}</td>
 * <td>Various information. Some of it is compression method-specific.</td>
 * </tr>
 * <tr>
 * <td>Comment</td>
 * <td>A text comment for the Zip entry.</td>
 * </tr>
 * <tr>
 * <td>External file attributes</td>
 * <td>Attributes for the entry. There are several different ways of
 * interpreting these attributes. Some of the ways are included in this package
 * as {@link ZipExternalFileAttributes} implementations.</td>
 * </tr>
 * <tr>
 * <td>Extra fields</td>
 * <td>Zero or more extra fields ({@link ZipEntryExtraField}:s) that contain
 * custom metadata for the entry. An extra field for an entry always has two
 * instances &ndash; the local header instance and the central directory
 * instance.</td>
 * </tr>
 * </table>
 * <p>
 * Zip entry objects are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipFile
 */
public abstract class ZipEntry extends AbstractArchiveEntry<ZipEntry, ZipDirectoryEntry>
{
	private static final UnsignedByte DEFAULT_VERSION_USED_TO_CREATE = UnsignedByte.valueOf(10);
	private static final UnsignedByte DEFAULT_VERSION_NEEDED_TO_EXTRACT = UnsignedByte.valueOf(10);
	private static final ZipGeneralPurposeBitFlags DEFAULT_GENERAL_PURPOSE_BIT_FLAGS = new ZipGeneralPurposeBitFlags();
	private static final UnsignedShort DEFAULT_DISK_NUMBER_START = UnsignedShort.valueOf(0);

	private final ZipExternalFileAttributes m_externalFileAttributes;
	private final UnsignedByte m_versionUsedToCreate;
	private final UnsignedByte m_versionNeededToExtract;
	private final ZipGeneralPurposeBitFlags m_generalPurposeBitFlags;
	private final Date m_lastModificationTime;
	private final UnsignedShort m_diskNumberStart;
	// The Zip entry's extra fields from both the local header and the
	// central header. This collection is unmodifiable.
	private final Collection<ZipEntryExtraField> m_extraFields;
	private final String m_comment;

	/**
	 * Create a new Zip entry. This constructor is only used for Zip directory
	 * entries that don't have an actual entry in the Zip file, but that has
	 * child entries anyway.
	 * <p>
	 * The Zip entry is assigned default values for all of its properties:
	 * <table>
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * </tr>
	 * <tr>
	 * <td>External file attributes</td>
	 * <td>{@link MsDosExternalFileAttributes#DEFAULT_DIRECTORY_ATTRIBUTES}</td>
	 * </tr>
	 * <tr>
	 * <td>Version used to create</td>
	 * <td>10 (meaning 1.0)</td>
	 * </tr>
	 * <tr>
	 * <td>Version needed to extract</td>
	 * <td>10 (meaning 1.0)</td>
	 * </tr>
	 * <tr>
	 * <td>General purpose bit flags</td>
	 * <td><i>empty</i></td>
	 * </tr>
	 * <tr>
	 * <td>Last modification time</td>
	 * <td>The current time</td>
	 * </tr>
	 * <tr>
	 * <td>Extra fields</td>
	 * <td><i>none</i></td>
	 * </tr>
	 * <tr>
	 * <td>Comment</td>
	 * <td><i>Empty string</i></td>
	 * </tr>
	 * </table>
	 * @param collaborator The parent Zip archive's entry collaborator.
	 * @param loc The location of the entry in the Zip file.
	 */
	protected ZipEntry(ZipEntryCollaborator collaborator, AbsoluteLocation loc)
	{
		super(loc, collaborator);

		m_externalFileAttributes = MsDosExternalFileAttributes.DEFAULT_DIRECTORY_ATTRIBUTES;
		m_versionUsedToCreate = DEFAULT_VERSION_USED_TO_CREATE;
		m_versionNeededToExtract = DEFAULT_VERSION_NEEDED_TO_EXTRACT;
		m_generalPurposeBitFlags = DEFAULT_GENERAL_PURPOSE_BIT_FLAGS;
		m_lastModificationTime = new Date();
		m_diskNumberStart = DEFAULT_DISK_NUMBER_START;
		m_extraFields = Collections.emptyList();
		m_comment = "";
	}

	/**
	 * Create a new Zip entry.
	 * @param collaborator The parent Zip archive's entry collaborator.
	 * @param zecd Data parsed from the entry's central directory record.
	 * @param zeld Data parsed from the entry's local header.
	 */
	protected ZipEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld)
	{
		super(zecd.getLocation(), collaborator);

		// Null checks
		zecd.getClass();

		m_externalFileAttributes = zecd.getExternalFileAttributes();
		m_versionUsedToCreate = zecd.getVersionUsedToCreate();
		m_versionNeededToExtract = zecd.getVersionNeededToExtract();
		m_generalPurposeBitFlags = zecd.getGeneralPurposeBitFlags();
		m_lastModificationTime = zecd.getLastModificationTime();
		m_diskNumberStart = zecd.getDiskNumberStart();
		ArrayList<ZipEntryExtraField> l = new ArrayList<ZipEntryExtraField>(zecd.getExtraFields().size() + zeld.getExtraFields().size());
		l.addAll(zecd.getExtraFields());
		l.addAll(zeld.getExtraFields());
		m_extraFields = Collections.unmodifiableList(l);
		m_comment = zecd.getFileComment();
	}

	/**
	 * Get the PK-Zip version (or equivalent) that was used to create this Zip
	 * entry. The version method is the version * 10, i.e. if this method
	 * returns 23, that corresponds to the version 2.3
	 * @return The PK-Zip version (or equivalent) used to create this entry.
	 */
	public UnsignedByte getVersionUsedToCreate()
	{
		return m_versionUsedToCreate;
	}

	/**
	 * Get the earliest PK-Zip version that can extract this Zip entry. The
	 * version number returned from this method is the version * 10, i.e. if
	 * this method returns 23, that corresponds to the version 2.3
	 * @return The earliest PK-Zip version that can extract this entry * 10.
	 */
	public UnsignedByte getVersionNeededToExtract()
	{
		return m_versionNeededToExtract;
	}

	/**
	 * Get the Zip entry's general purpose bit flags. Many of the bit flag
	 * values can be accessed directly on a Zip entry object. See
	 * {@link #isEncrypted()}, {@link ZipFileEntry#isCompressedPatchData()} and
	 * {@link #isStrongEncryption()}.
	 * @return The Zip entry's general purpose bit flags.
	 */
	public ZipGeneralPurposeBitFlags getGeneralPurposeBitFlags()
	{
		return m_generalPurposeBitFlags;
	}

	/**
	 * Is the Zip entry encrypted?
	 * @return {@code true} if the Zip entry is encrypted.
	 * @see #isStrongEncryption()
	 */
	public boolean isEncrypted()
	{
		return m_generalPurposeBitFlags.isEncrypted();
	}

	/**
	 * Is the Zip entry encrypted with strong encryption?
	 * @return {@code true} if the Zip entry is encrypted with strong
	 * encryption.
	 * @see #isEncrypted()
	 */
	public boolean isStrongEncryption()
	{
		return m_generalPurposeBitFlags.isStrongEncryption();
	}

	/**
	 * Get the instant in time when the file or directory entity that was used
	 * to create this Zip entry was last modified. The time zone for the
	 * returned time is the time zone for the computer that built the Zip file,
	 * i.e. often not UTC.
	 * <p>
	 * The {@link org.at4j.zip.ef.ExtendedTimestampExtraField}, if present, has
	 * the last modified time in the UTC time zone.
	 * @return The last modification time for the Zip entry, in the time zone
	 * for the computer that built the Zip file.
	 */
	public Date getLastModified()
	{
		// Defensive copy
		return m_lastModificationTime != null ? new Date(m_lastModificationTime.getTime()) : null;
	}

	/**
	 * Get the number of the disk where this Zip entry begins. The first disk is
	 * number 0.
	 * @return The number of the disk where this Zip entry begins.
	 */
	public UnsignedShort getDiskNumberStart()
	{
		return m_diskNumberStart;
	}

	/**
	 * Get the external file attributes value.
	 * @return The external file attributes.
	 */
	public ZipExternalFileAttributes getExternalFileAttributes()
	{
		return m_externalFileAttributes;
	}

	/**
	 * Get a read only collection containing the Zip entry's extra fields.
	 * @return The Zip entry's extra fields, from both the local header and this
	 * entry's record in the central directory.
	 */
	public Collection<ZipEntryExtraField> getExtraFields()
	{
		// This collection is unmodifiable
		return m_extraFields;
	}

	/**
	 * Get the first extra field found that matches the description.
	 * @param <T> The type of the extra field.
	 * @param type The class of the extra field, or a superclass.
	 * @param localHeader Should the extra field be from the local or the
	 * central header?
	 * @return The first extra field found that matches the description, or
	 * {@code null} if no extra field matches it.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getExtraField(Class<T> type, boolean localHeader)
	{
		for (ZipEntryExtraField ef : m_extraFields)
		{
			if ((ef.isInLocalHeader() == localHeader) && (type.isAssignableFrom(ef.getClass())))
			{
				return (T) ef;
			}
		}
		return null;
	}

	/**
	 * Get the Zip entry's comment.
	 * @return The Zip entry's comment, or an empty string if the comment was
	 * not set.
	 */
	public String getComment()
	{
		return m_comment;
	}
}
