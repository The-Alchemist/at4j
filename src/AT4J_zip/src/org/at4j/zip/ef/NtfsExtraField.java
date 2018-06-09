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
package org.at4j.zip.ef;

import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.util.WinNtTime;
import org.at4j.zip.builder.ZipBuilderConfiguration;

/**
 * This is the PKWare NTFS extra field. It contains information on the creation,
 * last access and last modification times of the entity.
 * @author Karl Gustafsson
 * @since 1.0
 * @see NtfsExtraFieldFactory
 */
public class NtfsExtraField implements ZipEntryExtraField
{
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0x000a);

	private static final byte[] TAG1 = UnsignedShort.valueOf(1).getBigEndianByteArray();
	private static final byte[] TAG1_SIZE = UnsignedShort.valueOf(24).getBigEndianByteArray();

	private final boolean m_inLocalHeader;
	private final WinNtTime m_lastModificationTime;
	private final WinNtTime m_lastAccessTime;
	private final WinNtTime m_creationTime;

	/**
	 * Create a new NTFS extra field.
	 * @param inLocalHeader Is the extra field in the Zip entry's local header
	 * or in the Zip file's central directory.
	 * @param lastModificationTime The last modification time for the file
	 * system entity that was used to create the Zip entry.
	 * @param lastAccessTime The last access time for the file system entity
	 * that was used to create the Zip entry.
	 * @param creationTime The creation time for the file system entity that was
	 * used to create the Zip entry.
	 */
	public NtfsExtraField(boolean inLocalHeader, WinNtTime lastModificationTime, WinNtTime lastAccessTime, WinNtTime creationTime)
	{
		// Null checks
		lastModificationTime.getClass();
		lastAccessTime.getClass();
		creationTime.getClass();

		m_inLocalHeader = inLocalHeader;
		m_lastModificationTime = lastModificationTime;
		m_lastAccessTime = lastAccessTime;
		m_creationTime = creationTime;
	}

	public boolean isInLocalHeader()
	{
		return m_inLocalHeader;
	}

	/**
	 * Get the time that the entity in the Zip entry was last modified.
	 * @return The last modification time.
	 */
	public WinNtTime getLastModified()
	{
		return m_lastModificationTime;
	}

	/**
	 * Get the time that the entity in the Zip entry was created.
	 * @return The creation time.
	 */
	public WinNtTime getCreationTime()
	{
		return m_creationTime;
	}

	/**
	 * Get the time when the entity in the Zip entry was last accessed.
	 * @return The last access time.
	 */
	public WinNtTime getLastAccessTime()
	{
		return m_lastAccessTime;
	}

	public byte[] encode(ZipBuilderConfiguration builder)
	{
		byte[] res = new byte[32];

		// "Reserved for future use"
		res[0] = (byte) 0;
		res[1] = (byte) 0;
		res[2] = (byte) 0;
		res[3] = (byte) 0;

		// Tag 1 value
		res[4] = TAG1[0];
		res[5] = TAG1[1];

		// Tag 1 size
		res[6] = TAG1_SIZE[0];
		res[7] = TAG1_SIZE[1];

		// Last modification time
		System.arraycopy(m_lastModificationTime.getTime().getBigEndianByteArray(), 0, res, 8, 8);

		// Last access time
		System.arraycopy(m_lastAccessTime.getTime().getBigEndianByteArray(), 0, res, 16, 8);

		// Creation time
		System.arraycopy(m_creationTime.getTime().getBigEndianByteArray(), 0, res, 24, 8);

		return res;
	}

	@Override
	public boolean equals(Object o)
	{
		//		System.out.println(m_lastModificationTime.getDate());
		//		System.out.println(m_lastAccessTime.getDate());
		//		System.out.println(m_creationTime.getDate());
		if ((o != null) && (o instanceof NtfsExtraField))
		{
			NtfsExtraField f2 = (NtfsExtraField) o;
			return (m_inLocalHeader == f2.m_inLocalHeader) && (m_lastModificationTime.equals(f2.m_lastModificationTime)) && (m_lastAccessTime.equals(f2.m_lastAccessTime)) && (m_creationTime.equals(f2.m_creationTime));
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int res = m_lastModificationTime.hashCode();
		res = m_lastAccessTime.hashCode() + 17 * res;
		res = m_creationTime.hashCode() + 17 * res;
		return m_inLocalHeader ? res + 1 : res;
	}

	@Override
	public String toString()
	{
		return "NTFS extra field in " + (m_inLocalHeader ? "local" : "central") + " file header: Last mod time " + m_lastModificationTime + ", last access time " + m_lastAccessTime + ", creation time " + m_creationTime;
	}
}
