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

import java.util.Date;
import java.util.Map;

import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This object contains data that is found in a Tar header.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class TarEntryHeaderData implements TarEntryHeader
{
	private AbsoluteLocation m_location;
	private boolean m_directory;
	private UnixEntityMode m_mode;
	private int m_ownerUid;
	private int m_ownerGid;
	private long m_fileSize;
	private Date m_lastModificationTime;
	private int m_checksum;
	private char m_typeFlag;
	private String m_linkName;
	private String m_magic;
	private String m_ustarVersion;
	private String m_ownerUserName;
	private String m_ownerGroupName;
	private int m_devMajor;
	private int m_devMinor;
	// Variables set in a previous PAX header
	private Map<String, String> m_paxVariables;

	public AbsoluteLocation getLocation()
	{
		return m_location;
	}

	public void setLocation(AbsoluteLocation location)
	{
		m_location = location;
	}

	public UnixEntityMode getMode()
	{
		return m_mode;
	}

	public void setMode(UnixEntityMode mode)
	{
		m_mode = mode;
	}

	public int getOwnerUid()
	{
		return m_ownerUid;
	}

	public void setOwnerUid(int ownerUid)
	{
		m_ownerUid = ownerUid;
	}

	public int getOwnerGid()
	{
		return m_ownerGid;
	}

	public void setOwnerGid(int ownerGid)
	{
		m_ownerGid = ownerGid;
	}

	public long getFileSize()
	{
		return m_fileSize;
	}

	public void setFileSize(long fileSize)
	{
		m_fileSize = fileSize;
	}

	public Date getLastModificationTime()
	{
		// Defensive copy
		return m_lastModificationTime != null ? new Date(m_lastModificationTime.getTime()) : null;
	}

	public void setLastModificationTime(Date lastModificationTime)
	{
		// Defensive copy
		m_lastModificationTime = new Date(lastModificationTime.getTime());
	}

	public int getChecksum()
	{
		return m_checksum;
	}

	public void setChecksum(int checksum)
	{
		m_checksum = checksum;
	}

	/**
	 * Get the type flag for the entry. This may be {@code '0'} (
	 * {@link TarConstants#FILE_TYPE_FLAG}) or {@code '\0'} (
	 * {@link TarConstants#ALT_FILE_TYPE_FLAG}) for a file, {@code '5'} (
	 * {@link TarConstants#DIRECTORY_TYPE_FLAG}) for a directory or {@code '2'}
	 * ({@link TarConstants#SYMBOLIC_LINK_TYPE_FLAG}) for a symbolic link. In
	 * the case of directories, the {@link #isDirectory()} method should
	 * preferably be used since the type flag does not seem to always be set.
	 */
	public char getTypeFlag()
	{
		return m_typeFlag;
	}

	public void setTypeFlag(char typeFlag)
	{
		m_typeFlag = typeFlag;
	}

	public String getLinkName()
	{
		return m_linkName;
	}

	public void setLinkName(String linkName)
	{
		m_linkName = linkName;
	}

	public String getMagic()
	{
		return m_magic;
	}

	public void setMagic(String magic)
	{
		m_magic = magic;
	}

	public String getUstarVersion()
	{
		return m_ustarVersion;
	}

	public void setUstarVersion(String ustarVersion)
	{
		m_ustarVersion = ustarVersion;
	}

	public String getOwnerUserName()
	{
		return m_ownerUserName;
	}

	public void setOwnerName(String ownerName)
	{
		m_ownerUserName = ownerName;
	}

	public String getOwnerGroupName()
	{
		return m_ownerGroupName;
	}

	public void setGroupName(String groupName)
	{
		m_ownerGroupName = groupName;
	}

	public int getDevMajor()
	{
		return m_devMajor;
	}

	public void setDevMajor(int devMajor)
	{
		m_devMajor = devMajor;
	}

	public int getDevMinor()
	{
		return m_devMinor;
	}

	public void setDevMinor(int devMinor)
	{
		m_devMinor = devMinor;
	}

	public boolean isDirectory()
	{
		return m_directory;
	}

	public void setDirectory(boolean directory)
	{
		m_directory = directory;
	}

	public Map<String, String> getVariables()
	{
		return m_paxVariables;
	}

	public void setVariables(Map<String, String> variables)
	{
		m_paxVariables = variables;
	}
}
