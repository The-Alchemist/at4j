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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import org.at4j.archive.builder.ArchiveEntryAddException;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.tar.TarConstants;
import org.entityfs.DataSink;
import org.entityfs.ETDirectory;
import org.entityfs.ETFile;
import org.entityfs.EntityType;
import org.entityfs.RandomAccess;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.io.DataSinkToOutputStreamAdapter;
import org.entityfs.util.io.RandomAccessToOutputStreamAdapter;

/**
 * This {@link TarEntryStrategy} creates Tar entries conforming to the old Unix
 * v7 format. The entries created by this strategy contain the following data:
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>Absolute location of the entry</td>
 * <td>Limited to 99 characters, excluding the initial slash.</td>
 * </tr>
 * <tr>
 * <td>{@link org.entityfs.entityattrs.unix.UnixEntityMode}</td>
 * <td>A Unix file permission mode such as {@code 0644} or {@code 0755}.</td>
 * </tr>
 * <tr>
 * <td>Owner UID</td>
 * <td>The user id of the owner of the file system entity that the Tar entry was
 * created from. This may be a value between {@code 0} and {@code 2097151}
 * (inclusive).</td>
 * </tr>
 * <tr>
 * <td>Owner GID</td>
 * <td>The group id of the group that owns the file system entity that the tar
 * entry was created from. This may be a value between {@code 0} and {@code
 * 2097151} (inclusive).</td>
 * </tr>
 * <tr>
 * <td>File size</td>
 * <td>For files, this is a value between {@code 0} and {@code 8589934591} bytes
 * (~ 8.6 Gb), inclusive.</td>
 * </tr>
 * <tr>
 * <td>Link name</td>
 * <td>For symbolic links, this is the link target. Limited to 99 characters.</td>
 * </tr>
 * </table>
 * @author Karl Gustafsson
 * @since 1.0
 */
public class V7TarEntryStrategy implements TarEntryStrategy
{
	/**
	 * This constant contains a Tar block filled with zeroes.
	 */
	private static final byte[] ZEROED_BLOCK = new byte[512];

	private final Charset m_textEncodingCharset;

	/**
	 * Create a new strategy object that will use the platform's default
	 * character encoding to encode text values in the Tar entries.
	 * @see #V7TarEntryStrategy(Charset)
	 */
	public V7TarEntryStrategy()
	{
		m_textEncodingCharset = Charset.defaultCharset();
	}

	/**
	 * Create a new strategy object that will use the supplied charset for
	 * encoding test values in the Tar entries.
	 * @param cs The charset.
	 * @see #V7TarEntryStrategy()
	 */
	public V7TarEntryStrategy(Charset cs)
	{
		// Null check
		cs.getClass();

		m_textEncodingCharset = cs;
	}

	/**
	 * Get the charset that is used to encode text values in Tar entries.
	 * @return The charset that is used to encode text values in Tar entries.
	 */
	protected final Charset getTextEncodingCharset()
	{
		return m_textEncodingCharset;
	}

	/**
	 * Add zeroes to the left of the supplied string so that it will have the
	 * requested size, and then convert the string to bytes using the platform's
	 * default charset. This should only be used for text containing characters
	 * that will occupy only one byte each.
	 * @param s The string to zero pad.
	 * @param size The requested size of the returned array.
	 * @return An array of the requested size containing the supplied text
	 * padded with zeroes to the left.
	 */
	protected byte[] zeroPadLeft(String s, int size)
	{
		byte[] res = new byte[size];
		Arrays.fill(res, (byte) '0');
		byte[] sarr = s.getBytes();
		System.arraycopy(sarr, 0, res, res.length - sarr.length, sarr.length);
		return res;
	}

	/**
	 * Set the current position of the {@code DataSink} to be on the next tar
	 * block boundary after a writing operation. Tar blocks are 512 bytes big.
	 * @param out The {@code RandomAccess}.
	 * @param sizeOfDataWritten The size of the data that was written. It is
	 * assumed that the write started on a block boundary.
	 * @throws WrappedIOException On I/O errors.
	 */
	protected void goToNextBlockBoundary(DataSink out, long sizeOfDataWritten) throws WrappedIOException
	{
		// Position the file at the next Tar block boundary
		if (sizeOfDataWritten > 0)
		{
			long nextBoundary = ((sizeOfDataWritten - 1) / TarConstants.BLOCK_SIZE + 1) * TarConstants.BLOCK_SIZE;
			int distanceToNextBoundary = (int) (nextBoundary - sizeOfDataWritten);
			if (distanceToNextBoundary > 0)
			{
				out.write(ZEROED_BLOCK, 0, distanceToNextBoundary);
			}
		}
	}

	/**
	 * Get the file name for the supplied location, discarding the path up to
	 * the file name. If it is a directory, add a trailing slash.
	 * @param location The absolute location.
	 * @param directory Should the file name be for a directory?
	 * @return The file name.
	 */
	protected String getFileName(AbsoluteLocation location, boolean directory)
	{
		String name = location.getLocation().substring(1);
		if (directory)
		{
			name += "/";
		}
		return name;
	}

	/**
	 * Set the file name field in the Tar entry header. This implementation
	 * throws an {@link ArchiveEntryAddException} if the file name is longer
	 * than 99 bytes. Subclasses may override this method to implement a
	 * different behavior.
	 * @param header The header to add the file name to.
	 * @param fileName The file name to add.
	 * @throws ArchiveEntryAddException If the file name is longer than 99
	 * bytes.
	 */
	protected void setFileNameInHeader(byte[] header, String fileName) throws ArchiveEntryAddException
	{
		byte[] nameBytes = Charsets.getBytes(fileName, m_textEncodingCharset);
		if (nameBytes.length > 99)
		{
			throw new ArchiveEntryAddException("The entity name " + fileName + " is longer than 99 bytes when encoded in the " + m_textEncodingCharset + " charset");
		}
		System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
	}

	/**
	 * Create a Tar header for the new entry. The header is a 512 bytes long
	 * byte array that contains metadata about the file system entity (file,
	 * directory, ...) that the Tar entry was created from.
	 * <p>
	 * This implementation creates a Unix v7 header (of course). Subclasses may
	 * override this method to create other kinds of headers.
	 * <p>
	 * This method uses the {@link #setFileNameInHeader(byte[], String)} method
	 * to set the file name.
	 * @param entity The entity that the Tar entry should represent. This may be
	 * a file, a directory or an {@link InputStream}.
	 * @param fileName The name of the entity.
	 * @param effectiveSettings Effective settings for the entry.
	 * @param fileSize If the entry is a file entry, this value contains the
	 * file size in bytes.
	 * @param entityType The type of the entity being added.
	 * @param lastModified When the entity was last modified.
	 * @return A 512 byte long array, without the checksum field filled in. The
	 * {@link #addChecksumToHeader(byte[])} method may be used to calculate the
	 * header's checksum when all data has been added to it.
	 * @throws ArchiveEntryAddException If the entry cannot be created, for some
	 * reason.
	 */
	protected byte[] getHeader(Object entity, String fileName, TarEntrySettings effectiveSettings, long fileSize, EntityType entityType, Date lastModified) throws ArchiveEntryAddException
	{
		byte[] header = new byte[512];

		// File name
		setFileNameInHeader(header, fileName);

		// Entity mode
		System.arraycopy(zeroPadLeft(Integer.toOctalString(effectiveSettings.getEntityMode().getCode()), 7), 0, header, 100, 7);

		// Owner uid
		System.arraycopy(zeroPadLeft(Integer.toOctalString(effectiveSettings.getOwnerUid().intValue()), 7), 0, header, 108, 7);

		// Owner gid
		System.arraycopy(zeroPadLeft(Integer.toOctalString(effectiveSettings.getOwnerGid().intValue()), 7), 0, header, 116, 7);

		// File length
		System.arraycopy(zeroPadLeft(Long.toOctalString(fileSize), 11), 0, header, 124, 11);

		// File modify time
		long modifyTime = lastModified.getTime() / 1000;
		System.arraycopy(zeroPadLeft(Long.toOctalString(modifyTime), 11), 0, header, 136, 11);

		// Checksum (skip)

		// Link indicator (always zero)

		// Link name is empty

		// Dev minor and dev major must be set to zeroes in order for Gnu Tar to
		// understand them. Can't find anything about this in the documentation.
		byte[] sevenZeroes = new byte[7];
		Arrays.fill(sevenZeroes, (byte) '0');
		System.arraycopy(sevenZeroes, 0, header, 329, 7);
		System.arraycopy(sevenZeroes, 0, header, 337, 7);

		return header;
	}

	/**
	 * Write the contents of the supplied file to the archive. This is used to
	 * write the file data for file entries.
	 * @param out The {@code RandomAccess} to write to. When this method is
	 * called, it should be positioned at the start of the first block of the
	 * file data (just after the file entry header). When this method returns,
	 * the {@code RandomAccess} is positioned just after the written data. (I.e:
	 * probably not at the next Tar block boundary). If this method throws an
	 * exception, the position of the {@code RandomAccess} is unspecified.
	 * @return The size of the written file.
	 * @throws WrappedIOException On I/O errors.
	 */
	protected long writeFileToArchive(DataSink out, ReadableFile f, TarEntrySettings effectiveSettings) throws WrappedIOException
	{
		try
		{
			InputStream is = f.openForRead();
			try
			{
				return StreamUtil.copyStreams(is, new DataSinkToOutputStreamAdapter(out), 16384);
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	/**
	 * Add the checksum field to the Tar entry header. The checksum is the sum
	 * of all individual bytes in the header, with the checksum field itself
	 * taken to be all spaces.
	 * <p>
	 * This method should be called after all data has been added to the tar
	 * entry header.
	 * @param header The Tar entry header.
	 * @return The Tar entry header. (The same byte array as {@code header}.)
	 */
	protected byte[] addChecksumToHeader(byte[] header)
	{
		// This checksum will only be six characters wide + \0 + ' '. That does
		// not fit the description in the documentation, but it is the way that
		// it looks in a reverse-engineered file. What's more: it seems to work.
		int checksum = 0;
		for (int i = 0; i < header.length; i++)
		{
			checksum += header[i] & 0xFF;
		}

		// Include the checksum field as eight spaces
		checksum += 8 * 0x20;

		System.arraycopy(zeroPadLeft(Long.toOctalString(checksum), 6), 0, header, 148, 6);
		header[155] = (byte) ' ';
		return header;
	}

	public void writeFile(DataSink out, ReadableFile f, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		byte[] header = getHeader(f, getFileName(location, false), effectiveSettings, f.getDataSize(), ETFile.TYPE, lastModified);
		addChecksumToHeader(header);
		out.write(header);
		long fileSize = writeFileToArchive(out, f, effectiveSettings);
		goToNextBlockBoundary(out, fileSize);
	}

	public void writeFileFromStream(RandomAccess out, InputStream is, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		// Skip the header and write the file first
		long startPos = out.getFilePointer();
		long fileSize = 0;
		boolean successful = false;
		try
		{
			out.seek(startPos + TarConstants.BLOCK_SIZE);
			fileSize = StreamUtil.copyStreams(is, new RandomAccessToOutputStreamAdapter(out, false), 16384);
			long endPos = out.getFilePointer();
			byte[] header = getHeader(is, getFileName(location, false), effectiveSettings, fileSize, ETFile.TYPE, lastModified);
			addChecksumToHeader(header);
			out.seek(startPos);
			out.write(header);
			out.seek(endPos);
			successful = true;
		}
		finally
		{
			if (successful)
			{
				goToNextBlockBoundary(out, fileSize);
			}
			else
			{
				// Did not succeed in writing the record. Truncate the file to
				// restore it to what it was before this attempt
				out.setLength(startPos);
				out.seek(startPos);
			}
		}
	}

	public void writeDirectory(DataSink out, DirectoryAdapter<?> da, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		out.write(addChecksumToHeader(getHeader(da, getFileName(location, true), effectiveSettings, 0, ETDirectory.TYPE, lastModified)));
		// Don't have to go to the next block boundary since we're already there
		// (The directory consists of just a header.)
	}
}
