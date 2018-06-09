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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.at4j.archive.ArchiveSymbolicLinkEntry;
import org.at4j.support.lang.UnsignedInteger;
import org.at4j.zip.comp.ZipEntryCompressionMethod;
import org.entityfs.el.EntityLocation;
import org.entityfs.el.EntityLocations;

/**
 * This object represents a symbolic link entry in a Zip archive. In addition to
 * the properties inherited from {@link ZipEntry}, a symbolic link has a link
 * target and a CRC 32 checksum calculated on the link value.
 * <p>
 * Zip entries are always immutable.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipDirectoryEntry
 * @see ZipFileEntry
 */
public class ZipSymbolicLinkEntry extends ZipEntry implements ArchiveSymbolicLinkEntry<ZipEntry, ZipDirectoryEntry>
{
	private final EntityLocation<?> m_target;
	private final UnsignedInteger m_crc32;

	/**
	 * Create a symbolic link entry.
	 * @param collaborator The parent Zip archive's entry collaborator.
	 * @param zecd Data parsed from the Zip entry's central directory record.
	 * @param zeld Data parsed from the Zip entry's local header.
	 * @param cs The charset to use when decoding the link.
	 */
	public ZipSymbolicLinkEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld, Charset cs)
	{
		super(collaborator, zecd, zeld);
		// Read the link target.

		// This is most likely "stored", but we probably cannot safely assume
		// that for all entries.
		ZipEntryCompressionMethod cm = zecd.getCompressionMethod();
		try
		{
			InputStream is = cm.createInputStream(getCollaborator().openStream(zeld.getStartPositionOfFileData(), zeld.getStartPositionOfFileData() + zecd.getCompressedSize().longValue() - 1), zecd.getCompressedSize().longValue(), zecd
					.getUncompressedSize().longValue());
			try
			{
				int noToRead = (int) zecd.getUncompressedSize().longValue();
				int pos = 0;
				byte[] barr = new byte[noToRead];
				while (pos < noToRead)
				{
					pos += is.read(barr, pos, noToRead - pos);
				}
				m_target = EntityLocations.parseLocation(cs.decode(ByteBuffer.wrap(barr)).toString());
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
			throw new ZipFileParseException(e);
		}
		m_crc32 = zecd.getCrc32();
	}

	/**
	 * Get the symbolic link's target. This is either an
	 * {@link org.entityfs.el.AbsoluteLocation} or a
	 * {@link org.entityfs.el.RelativeLocation}.
	 * @return The symbolic link's target.
	 */
	public EntityLocation<?> getLinkTarget()
	{
		return m_target;
	}

	/**
	 * Get the CRC 32 checksum computed over the link target location.
	 * @return The CRC32 checksum.
	 */
	public UnsignedInteger getCrc32()
	{
		return m_crc32;
	}

	@Override
	public String toString()
	{
		return getName() + " (l)";
	}
}
