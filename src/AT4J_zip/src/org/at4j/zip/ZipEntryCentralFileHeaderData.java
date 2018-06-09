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

import java.util.Collection;
import java.util.Date;

import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.comp.ZipEntryCompressionMethod;
import org.at4j.zip.ef.ZipEntryExtraField;
import org.at4j.zip.extattrs.ZipExternalFileAttributes;
import org.entityfs.el.AbsoluteLocation;

/**
 * This bean contains the data stored about a Zip entry in the Zip file central
 * directory.. It is used by the {@link ZipFileParser} to create an appropriate
 * {@link ZipEntry} object from.
 * <p>
 * This is part of the {@link ZipFile} implementations. Clients should not have
 * to bother with this object.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntryLocalFileHeaderData
 */
public class ZipEntryCentralFileHeaderData
{
	private UnsignedByte m_versionUsedToCreate;
	private UnsignedByte m_versionNeededToExtract;
	private ZipGeneralPurposeBitFlags m_generalPurposeBitFlags;
	private ZipEntryCompressionMethod m_compressionMethod;
	private Date m_lastModificationTime;
	private UnsignedInteger m_crc32;
	private UnsignedInteger m_compressedSize;
	private UnsignedInteger m_uncompressedSize;
	private UnsignedShort m_diskNumberStart;
	private ZipInternalFileAttributes m_internalFileAttributes;
	private ZipExternalFileAttributes m_externalFileAttributes;
	private UnsignedInteger m_relativeOffsetOfLocalHeader;
	private AbsoluteLocation m_location;
	private Collection<ZipEntryExtraField> m_extraFields;
	private String m_fileComment;
	private boolean m_directory;

	public void setVersionUsedToCreate(UnsignedByte versionUsedToCreate)
	{
		m_versionUsedToCreate = versionUsedToCreate;
	}

	public UnsignedByte getVersionUsedToCreate()
	{
		return m_versionUsedToCreate;
	}

	public UnsignedByte getVersionNeededToExtract()
	{
		return m_versionNeededToExtract;
	}

	public void setVersionNeededToExtract(UnsignedByte versionNeededToExtract)
	{
		m_versionNeededToExtract = versionNeededToExtract;
	}

	public ZipGeneralPurposeBitFlags getGeneralPurposeBitFlags()
	{
		return m_generalPurposeBitFlags;
	}

	public void setGeneralPurposeBitFlags(ZipGeneralPurposeBitFlags generalPurposeBitFlags)
	{
		m_generalPurposeBitFlags = generalPurposeBitFlags;
	}

	public ZipEntryCompressionMethod getCompressionMethod()
	{
		return m_compressionMethod;
	}

	public void setCompressionMethod(ZipEntryCompressionMethod compressionMethod)
	{
		m_compressionMethod = compressionMethod;
	}

	public Date getLastModificationTime()
	{
		return m_lastModificationTime;
	}

	public void setLastModificationTime(Date lastModificationTime)
	{
		// Defensive copy
		m_lastModificationTime = lastModificationTime != null ? new Date(lastModificationTime.getTime()) : null;
	}

	public UnsignedInteger getCrc32()
	{
		return m_crc32;
	}

	public void setCrc32(UnsignedInteger crc32)
	{
		m_crc32 = crc32;
	}

	public UnsignedInteger getCompressedSize()
	{
		return m_compressedSize;
	}

	public void setCompressedSize(UnsignedInteger compressedSize)
	{
		m_compressedSize = compressedSize;
	}

	public UnsignedInteger getUncompressedSize()
	{
		return m_uncompressedSize;
	}

	public void setUncompressedSize(UnsignedInteger uncompressedSize)
	{
		m_uncompressedSize = uncompressedSize;
	}

	public UnsignedShort getDiskNumberStart()
	{
		return m_diskNumberStart;
	}

	public void setDiskNumberStart(UnsignedShort diskNumberStart)
	{
		m_diskNumberStart = diskNumberStart;
	}

	public ZipInternalFileAttributes getInternalFileAttributes()
	{
		return m_internalFileAttributes;
	}

	public void setInternalFileAttributes(ZipInternalFileAttributes internalFileAttributes)
	{
		m_internalFileAttributes = internalFileAttributes;
	}

	public ZipExternalFileAttributes getExternalFileAttributes()
	{
		return m_externalFileAttributes;
	}

	public void setExternalFileAttributes(ZipExternalFileAttributes externalFileAttributes)
	{
		m_externalFileAttributes = externalFileAttributes;
	}

	public UnsignedInteger getRelativeOffsetOfLocalHeader()
	{
		return m_relativeOffsetOfLocalHeader;
	}

	public void setRelativeOffsetOfLocalHeader(UnsignedInteger relativeOffsetOfLocalHeader)
	{
		m_relativeOffsetOfLocalHeader = relativeOffsetOfLocalHeader;
	}

	public AbsoluteLocation getLocation()
	{
		return m_location;
	}

	public void setLocation(AbsoluteLocation location)
	{
		m_location = location;
	}

	public Collection<ZipEntryExtraField> getExtraFields()
	{
		return m_extraFields;
	}

	public void setExtraFields(Collection<ZipEntryExtraField> extraFields)
	{
		m_extraFields = extraFields;
	}

	public String getFileComment()
	{
		return m_fileComment;
	}

	public void setFileComment(String fileComment)
	{
		m_fileComment = fileComment;
	}

	public boolean isDirectory()
	{
		return m_directory;
	}

	public void setDirectory(boolean directory)
	{
		m_directory = directory;
	}
}
