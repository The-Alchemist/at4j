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
package org.at4j.tar.builder;

import java.nio.charset.Charset;
import java.util.Date;

import org.at4j.archive.builder.ArchiveEntryAddException;
import org.at4j.support.nio.charset.Charsets;
import org.entityfs.ETDirectory;
import org.entityfs.ETFile;
import org.entityfs.EntityType;

/**
 * This strategy extends the {@link V7TarEntryStrategy} to be able to write full
 * ustar Tar entry headers. In addition to the data in a v7 header, the ustar
 * header contains the following data:
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>Version of the ustar header</td>
 * <td>This is set to 00</td>
 * </tr>
 * <tr>
 * <td>Owner name</td>
 * <td>The name of the user owning the entity that the Tar entry was created
 * from. The maximum size of this field is 31 8-byte characters.</td>
 * </tr>
 * <tr>
 * <td>Group name</td>
 * <td>The name of the group owning the entity that the Tar entry was created
 * from. The maximum size of this field is 31 8-byte characters.</td>
 * </tr>
 * <tr>
 * <td>Device major number</td>
 * <td><i>This is always set to 0000000 by this implementation.</i></td>
 * </tr>
 * <tr>
 * <td>Device minor number</td>
 * <td><i>This is always set to 0000000 by this implementation.</i></td>
 * </tr>
 * <tr>
 * <td>Name prefix</td>
 * <td>This is 155 additional bytes that can be prefixed to the entry name if it
 * is longer than 99 characters. This gives a theoretical maximum path length of
 * 255 characters, but since the split must occur at a slash, the actual maximum
 * length is often shorter.</td>
 * </tr>
 * </table>
 * @author Karl Gustafsson
 * @since 1.0
 */
public class UstarEntryStrategy extends V7TarEntryStrategy
{
	private static final byte[] MAGIC_BYTES = "ustar".getBytes();
	private static final byte[] VERSION_BYTES = "00".getBytes();

	private static final byte FILE_TYPE = (byte) '0';
	private static final byte DIRECTORY_TYPE = (byte) '5';

	/**
	 * Create a new strategy object that will use the platform's default
	 * character encoding to encode text values in the Tar entries.
	 * @see #UstarEntryStrategy(Charset)
	 */
	public UstarEntryStrategy()
	{
		// Nothing
	}

	/**
	 * Create a new strategy object that will use the supplied charset for
	 * encoding test values in the Tar entries.
	 * @param cs The charset.
	 * @see #UstarEntryStrategy()
	 */
	public UstarEntryStrategy(Charset cs)
	{
		super(cs);
	}

	/**
	 * Override the inherited implementation to be able to split long file names
	 * between the prefix and the name fields.
	 * @throws ArchiveEntryAddException If the file name is too long.
	 */
	@Override
	protected void setFileNameInHeader(byte[] header, String fileName) throws ArchiveEntryAddException
	{
		byte[] nameBytes = Charsets.getBytes(fileName, getTextEncodingCharset());
		if (nameBytes.length < 100)
		{
			// The whole name fits in the old name record
			System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
		}
		else
		{
			// The name did not fit in the old name record. Try to split the
			// name at the last / and see if the two fragments fit in the old
			// name record and the name prefix record
			int slashIndex = fileName.lastIndexOf('/');
			if (slashIndex < 0)
			{
				throw new ArchiveEntryAddException("The name " + fileName + " does not fit in the name header");
			}

			String prefix = fileName.substring(0, slashIndex);
			String suffix = fileName.substring(slashIndex + 1);
			byte[] prefixBytes = Charsets.getBytes(prefix, getTextEncodingCharset());
			byte[] suffixBytes = Charsets.getBytes(suffix, getTextEncodingCharset());
			if ((prefixBytes.length > 155) || (suffixBytes.length > 99))
			{
				throw new ArchiveEntryAddException("The name " + fileName + " does not fit in the name header");
			}

			// Set the name in the old name record and the name prefix record
			System.arraycopy(suffixBytes, 0, header, 0, suffixBytes.length);
			System.arraycopy(prefixBytes, 0, header, 345, prefixBytes.length);
		}
	}

	/**
	 * Override the inherited implementation to add ustar specific data.
	 */
	@Override
	protected byte[] getHeader(Object entity, String fileName, TarEntrySettings effectiveSettings, long fileSize, EntityType entityType, Date lastModified) throws ArchiveEntryAddException
	{
		byte[] header = super.getHeader(entity, fileName, effectiveSettings, fileSize, entityType, lastModified);

		// Type flag
		if (entityType == ETFile.TYPE)
		{
			header[156] = FILE_TYPE;
		}
		else if (entityType == ETDirectory.TYPE)
		{
			header[156] = DIRECTORY_TYPE;
		}
		else
		{
			throw new ArchiveEntryAddException("The entity type " + entityType + " of entity " + entity + " is not supported");
		}

		// Magic
		System.arraycopy(MAGIC_BYTES, 0, header, 257, 5);

		// Version
		header[263] = VERSION_BYTES[0];
		header[264] = VERSION_BYTES[1];

		// User name
		byte[] userNameBytes = Charsets.getBytes(effectiveSettings.getOwnerUserName(), getTextEncodingCharset());
		System.arraycopy(userNameBytes, 0, header, 265, Math.min(32, userNameBytes.length));

		// Group name
		byte[] groupNameBytes = Charsets.getBytes(effectiveSettings.getOwnerGroupName(), getTextEncodingCharset());
		System.arraycopy(groupNameBytes, 0, header, 297, Math.min(32, groupNameBytes.length));

		return header;
	}
}
