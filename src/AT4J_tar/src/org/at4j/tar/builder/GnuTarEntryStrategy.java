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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import org.at4j.support.nio.charset.Charsets;
import org.entityfs.DataSink;
import org.entityfs.RandomAccess;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;

/**
 * This strategy extends the {@link UstarEntryStrategy} with the capability to
 * use special headers for long Tar entry paths and link targets.
 * <p>
 * Gnu Tar headers contain the same data as ustar headers. If the Tar entry path
 * for an entry is too long to fit in the ustar header, this strategy adds a
 * special file name header containing the Tar entry path before the regular
 * ustar entry header.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class GnuTarEntryStrategy extends UstarEntryStrategy
{
	// This contains everything except for the file length and the checksum.
	// The version is set to " \0"
	private static final byte[] GNU_LONG_FILE_NAME_HEADER_TEMPLATE = new byte[512];
	static
	{
		// Name
		byte[] longLink = "././@LongLink".getBytes();
		System.arraycopy(longLink, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 0, longLink.length);

		byte[] elevenZeroes = new byte[11];
		Arrays.fill(elevenZeroes, (byte) '0');

		// Mode
		System.arraycopy(elevenZeroes, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 100, 7);

		// UID
		System.arraycopy(elevenZeroes, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 108, 7);

		// GID
		System.arraycopy(elevenZeroes, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 116, 7);

		// MTime
		System.arraycopy(elevenZeroes, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 136, 11);

		// Type flag
		GNU_LONG_FILE_NAME_HEADER_TEMPLATE[156] = (byte) 'L';

		// Magic and version
		System.arraycopy("ustar  ".getBytes(), 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 257, 7);

		// UName
		byte[] rootBytes = "root".getBytes();
		System.arraycopy(rootBytes, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 265, 4);

		// GName
		System.arraycopy(rootBytes, 0, GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 297, 4);
	}

	/**
	 * Create a new strategy object that will use the platform's default
	 * character encoding to encode text values in the Tar entries.
	 * @see #GnuTarEntryStrategy(Charset)
	 */
	public GnuTarEntryStrategy()
	{
		// Nothing
	}

	/**
	 * Create a new strategy object that will use the supplied charset for
	 * encoding test values in the Tar entries.
	 * @param cs The charset.
	 * @see #GnuTarEntryStrategy()
	 */
	public GnuTarEntryStrategy(Charset cs)
	{
		super(cs);
	}

	private void writeGnuLongFileNameHeader(DataSink out, byte[] fileName)
	{
		// Copy the template
		byte[] header = new byte[512];
		System.arraycopy(GNU_LONG_FILE_NAME_HEADER_TEMPLATE, 0, header, 0, 512);

		// Fill in the file name length
		System.arraycopy(zeroPadLeft(Integer.toOctalString(fileName.length), 11), 0, header, 124, 11);

		// Calculate the checksum
		addChecksumToHeader(header);

		// Write
		out.write(header);
		out.write(fileName);

		goToNextBlockBoundary(out, header.length + fileName.length);
	}

	private AbsoluteLocation addLongFileNameHeaderIfNecessary(DataSink out, AbsoluteLocation location)
	{
		// A file name longer than 99 bytes?
		String fileName = getFileName(location, false);
		byte[] fileNameBytes = Charsets.getBytes(fileName, getTextEncodingCharset());
		if (fileNameBytes.length > 99)
		{
			// Create a Gnu long file name header
			writeGnuLongFileNameHeader(out, fileNameBytes);

			String truncatedFileName = getTextEncodingCharset().decode(ByteBuffer.wrap(fileNameBytes, 0, 99)).toString();
			while (truncatedFileName.endsWith("/"))
			{
				truncatedFileName = truncatedFileName.substring(0, truncatedFileName.length() - 1);
			}
			location = new AbsoluteLocation("/" + truncatedFileName);
		}
		return location;
	}

	/**
	 * Override the inherited implementation to prepend the Tar entry header
	 * with a special file name header if necessary.
	 */
	@Override
	public void writeFile(DataSink out, ReadableFile f, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		location = addLongFileNameHeaderIfNecessary(out, location);
		super.writeFile(out, f, location, effectiveSettings, lastModified);
	}

	/**
	 * Override the inherited implementation to prepend the Tar entry header
	 * with a special file name header if necessary.
	 */
	@Override
	public void writeFileFromStream(RandomAccess out, InputStream is, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		location = addLongFileNameHeaderIfNecessary(out, location);
		super.writeFileFromStream(out, is, location, effectiveSettings, lastModified);
	}

	/**
	 * Override the inherited implementation to prepend the Tar entry header
	 * with a special file name header if necessary.
	 */
	@Override
	public void writeDirectory(DataSink out, DirectoryAdapter<?> da, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		location = addLongFileNameHeaderIfNecessary(out, location);
		super.writeDirectory(out, da, location, effectiveSettings, lastModified);
	}
}
