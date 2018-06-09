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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.support.util.MsDosDate;
import org.at4j.support.util.MsDosTime;
import org.at4j.zip.comp.ZipEntryCompressionMethodRegistry;
import org.at4j.zip.ef.ZipEntryExtraField;
import org.at4j.zip.ef.ZipEntryExtraFieldParser;
import org.at4j.zip.ef.ZipEntryExtraFieldParserRegistry;
import org.at4j.zip.extattrs.UnixEntityType;
import org.at4j.zip.extattrs.UnixExternalFileAttributes;
import org.at4j.zip.extattrs.ZipExternalFileAttributesParserRegistry;
import org.entityfs.RandomAccess;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.exception.UnexpectedEofException;
import org.entityfs.support.exception.WrappedIOException;

/**
 * When it is created, a {@link ZipFile} uses a {@code ZipFileParser} instance
 * to parse the contents of a Zip file. The parser objects has a few extension
 * points that (advanced) clients may use to teach the parser about new extra
 * field types, new compression methods or new kinds of external file
 * attributes.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipFile
 */
public class ZipFileParser
{
	protected static final long LOCAL_FILE_HEADER = 0x04034b50;
	protected static final long CENTRAL_FILE_HEADER = 0x02014b50;

	private static final int[] END_OF_CENTRAL_DIRECTORY_HEADER_BACKWARDS = new int[] { 0x06, 0x05, 0x4b, 0x50 };
	// The maximum allowed size of the central directory header is somewhat
	// above 64k (the maximum size of the Zip file comment).
	private static final int BYTES_TO_SCAN_FOR_END_OF_CENTRAL_DIRECTORY_HEADER_BEFORE_GIVING_UP = 67000;

	private static final class ZipEntityHolder
	{
		private ZipEntryCentralFileHeaderData m_zecd;
		private ZipEntryLocalFileHeaderData m_zeld;
		private Map<String, ZipEntityHolder> m_childEntities = new HashMap<String, ZipEntityHolder>();
	}

	private static final class EndOfCentralDirectoryRecordContents
	{
		private UnsignedInteger m_startOfCentralDirectory;
		private String m_fileComment;
	}

	// Factory objects used to parse extra fields.
	private ZipEntryExtraFieldParserRegistry m_extraFieldParserRegistry = new ZipEntryExtraFieldParserRegistry();
	private ZipEntryCompressionMethodRegistry m_compressionMethodRegistry = new ZipEntryCompressionMethodRegistry();
	private ZipExternalFileAttributesParserRegistry m_externalFileAttrFactoryRegistry = new ZipExternalFileAttributesParserRegistry();

	/**
	 * Get the compression method registry used by this object.
	 * <p>
	 * The object returned is the actual registry used by this object. By
	 * changing its configuration, the behavior of this parser is changed.
	 * @return The compression method registry used by this object.
	 * @see #setCompressionMethodRegistry(ZipEntryCompressionMethodRegistry)
	 */
	public ZipEntryCompressionMethodRegistry getCompressionMethodRegistry()
	{
		return m_compressionMethodRegistry;
	}

	/**
	 * Set a custom compression method registry.
	 * <p>
	 * If this is not set, a registry with the default configuration for
	 * {@link ZipEntryCompressionMethodRegistry} is used.
	 * <p>
	 * The {@link #getCompressionMethodRegistry()} method can be used to modify
	 * the configuration of the current registry.
	 * @param registry The new compression method registry.
	 * @see #getCompressionMethodRegistry()
	 */
	public void setCompressionMethodRegistry(ZipEntryCompressionMethodRegistry registry)
	{
		// Null check
		registry.getClass();

		m_compressionMethodRegistry = registry;
	}

	/**
	 * Get the external file attributes parser registry used by this object.
	 * <p>
	 * The object returned is the actual registry used by this object. By
	 * changing its configuration, the behavior of this parser is changed.
	 * @return The external file attribute parser registry used by this object.
	 * @see #setExternalFileAttributesParserRegistry(ZipExternalFileAttributesParserRegistry)
	 */
	public ZipExternalFileAttributesParserRegistry getExternalFileAttributesParserRegistry()
	{
		return m_externalFileAttrFactoryRegistry;
	}

	/**
	 * Set a custom external file attributes parser registry.
	 * <p>
	 * If this is not set, a registry with the default configuration for
	 * {@link ZipExternalFileAttributesParserRegistry} is used.
	 * <p>
	 * The {@link #getExternalFileAttributesParserRegistry()} method can be used
	 * to modify the configuration of the current registry.
	 * @param registry The new registry.
	 * @see #getExternalFileAttributesParserRegistry()
	 */
	public void setExternalFileAttributesParserRegistry(ZipExternalFileAttributesParserRegistry registry)
	{
		// Null check
		registry.getClass();

		m_externalFileAttrFactoryRegistry = registry;
	}

	/**
	 * Get the extra field parser registry used by this object.
	 * <p>
	 * The object returned is the actual registry used by this object. By
	 * changing its configuration, the behavior of this object is changed.
	 * @return The extra field parser registry used by this object.
	 * @see #setExtraFieldParserRegistry(ZipEntryExtraFieldParserRegistry)
	 */
	public ZipEntryExtraFieldParserRegistry getExtraFieldParserRegistry()
	{
		return m_extraFieldParserRegistry;
	}

	/**
	 * Set a custom Zip entry extra field parser registry.
	 * <p>
	 * If this is not set, the default configuration for
	 * {@link ZipEntryExtraFieldParserRegistry} is used.
	 * <p>
	 * The {@link #getExtraFieldParserRegistry()} method can be used to modify
	 * the configuration of the current registry.
	 * @param registry The new registry.
	 * @see #getExtraFieldParserRegistry()
	 */
	public void setExtraFieldParserRegistry(ZipEntryExtraFieldParserRegistry registry)
	{
		// Null check
		registry.getClass();

		m_extraFieldParserRegistry = registry;
	}

	/**
	 * Does the integer contain the local Zip entry header magic number?
	 * @param i The integer.
	 * @return {@code true} if the integer contains the local Zip entry header
	 * magic number.
	 */
	protected boolean isLocalFileHeader(UnsignedInteger i)
	{
		return (i != null) && (i.longValue() == LOCAL_FILE_HEADER);
	}

	/**
	 * Does the integer contain the central directory header magic number?
	 * @param i The integer.
	 * @return {@code true} if the integer contains the central directory header
	 * magic number.
	 */
	protected boolean isCentralFileHeader(UnsignedInteger i)
	{
		return (i != null) && (i.longValue() == CENTRAL_FILE_HEADER);
	}

	/**
	 * This method throws a {@link ZipFileParseException} if the supplied
	 * integer's value is not the central directory header magic number.
	 * @param i The integer.
	 * @param ra The random access that the Zip file is read from.
	 * @throws ZipFileParseException If the supplied integer is not the central
	 * directory header magic number.
	 */
	protected void assertIsCentralFileHeader(UnsignedInteger i, RandomAccess ra) throws ZipFileParseException
	{
		if (i.longValue() != CENTRAL_FILE_HEADER)
		{
			throw new ZipFileParseException("Parse error at position " + (ra.getFilePointer() - UnsignedInteger.SIZE) + ". Expected a central file header");
		}
	}

	/**
	 * Read a string from the random access.
	 * @param ra The random access to read the string from.
	 * @param length The length of the string, in bytes.
	 * @param cs The charset to use for decoding the string, unless {@code
	 * utf8Encoding} is set.
	 * @param utf8Encoding If this is set, the string is decoded using the UTF-8
	 * charset no matter what the {@code cs} argument is set to.
	 * @return The string
	 * @throws UnexpectedEofException If the end of the file is reached before
	 * the string data has been fully read.
	 */
	protected String readString(RandomAccess ra, int length, Charset cs, boolean utf8Encoding) throws UnexpectedEofException
	{
		if (length == 0)
		{
			return "";
		}

		byte[] barr = new byte[length];
		int noRead = ra.read(barr);
		if (noRead != length)
		{
			throw new UnexpectedEofException("Wanted to read " + length + " bytes. Got " + noRead);
		}
		return (utf8Encoding ? Charsets.UTF8.decode(ByteBuffer.wrap(barr)) : cs.decode(ByteBuffer.wrap(barr))).toString();
	}

	/**
	 * Read a byte array from the random access.
	 * @param ra The random access to read the byte array from.
	 * @param length The length of the byte array.
	 * @return A new byte array containing data read from the random access.
	 * @throws UnexpectedEofException If the end of the file is reached before
	 * the byte array has been fully read.
	 */
	protected byte[] readByteArray(final RandomAccess ra, final int length) throws UnexpectedEofException
	{
		if (length == 0)
		{
			return new byte[0];
		}

		final byte[] barr = new byte[length];
		final int noRead = ra.read(barr);
		if (noRead != length)
		{
			throw new UnexpectedEofException("Wanted to read " + length + " bytes. Got " + noRead);
		}
		return barr;
	}

	private Collection<ZipEntryExtraField> parseExtraFields(final byte[] barr, final boolean inLocalHeader)
	{
		if (barr.length < (UnsignedShort.SIZE * 2))
		{
			return new ArrayList<ZipEntryExtraField>(0);
		}

		Collection<ZipEntryExtraField> res = new ArrayList<ZipEntryExtraField>(3);
		try
		{
			InputStream is = new ByteArrayInputStream(barr);
			try
			{
				while (is.available() > 0)
				{
					UnsignedShort header = UnsignedShort.readBigEndian(is);
					UnsignedShort dataSize = UnsignedShort.readBigEndian(is);
					byte[] extraFieldData = new byte[dataSize.intValue()];
					if (dataSize.intValue() > 0)
					{
						int noRead = is.read(extraFieldData);
						if (noRead != dataSize.intValue())
						{
							throw new ZipFileParseException("Wanted to read " + dataSize + " bytes of extra field data. Got " + noRead);
						}
					}

					ZipEntryExtraFieldParser zefp = m_extraFieldParserRegistry.forCode(header);
					res.add(zefp.parse(extraFieldData, inLocalHeader));
				}
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
		return res;
	}

	/**
	 * @return {@code null} if this entry should be ignored.
	 */
	private ZipEntryCentralFileHeaderData readCentralFileHeader(RandomAccess ra, Charset fileNameEncodingCs, Charset textEncodingCs)
	{
		// This is set to true if the header should be ignored.
		boolean shouldBeIgnored = false;
		
		ZipEntryCentralFileHeaderData zed = new ZipEntryCentralFileHeaderData();
		zed.setVersionUsedToCreate(UnsignedByte.read(ra));
		ZipVersionMadeBy vmb = ZipVersionMadeBy.valueOf(UnsignedByte.read(ra));
		// This is stored in two bytes in the file, but the value is only one
		// byte long
		zed.setVersionNeededToExtract(UnsignedByte.valueOf(UnsignedShort.readBigEndian(ra).intValue()));
		zed.setGeneralPurposeBitFlags(new ZipGeneralPurposeBitFlags(UnsignedShort.readBigEndian(ra)));
		zed.setCompressionMethod(m_compressionMethodRegistry.forCode(UnsignedShort.readBigEndian(ra)).create(zed.getGeneralPurposeBitFlags()));
		Calendar time = Calendar.getInstance();
		time.clear();
		MsDosTime.parseMsDosTime(UnsignedShort.readBigEndian(ra), time);
		MsDosDate.parseMsDosDate(UnsignedShort.readBigEndian(ra), time);
		zed.setLastModificationTime(time.getTime());
		zed.setCrc32(UnsignedInteger.readBigEndian(ra));
		zed.setCompressedSize(UnsignedInteger.readBigEndian(ra));
		zed.setUncompressedSize(UnsignedInteger.readBigEndian(ra));
		UnsignedShort fileNameLength = UnsignedShort.readBigEndian(ra);
		UnsignedShort extraFieldsLength = UnsignedShort.readBigEndian(ra);
		UnsignedShort fileCommentLength = UnsignedShort.readBigEndian(ra);
		zed.setDiskNumberStart(UnsignedShort.readBigEndian(ra));
		zed.setInternalFileAttributes(new ZipInternalFileAttributes(UnsignedShort.readBigEndian(ra)));
		byte[] externalFileAttrs = new byte[4];
		int noRead = ra.read(externalFileAttrs);
		if (noRead != 4)
		{
			throw new ZipFileParseException("Wanted to read 4 bytes. Got " + noRead);
		}
		zed.setExternalFileAttributes(m_externalFileAttrFactoryRegistry.forVersionMadeBy(vmb).parse(vmb, externalFileAttrs));
		zed.setRelativeOffsetOfLocalHeader(UnsignedInteger.readBigEndian(ra));
		boolean utf8Encoding = zed.getGeneralPurposeBitFlags().isUtf8Encoding();
		String fileName = readString(ra, fileNameLength.intValue(), fileNameEncodingCs, utf8Encoding);
		if ("/".equals(fileName))
		{
			// An empty root directory. Ignore this entry, but read the rest of
			// the header anyway so that the RandomAccess is positioned at the
			// next header when the method returns.
			shouldBeIgnored = true;
		}
		else if (fileName.endsWith("/"))
		{
			zed.setDirectory(true);
			fileName = fileName.substring(0, fileName.length() - 1);
		}
		else
		{
			zed.setDirectory(false);
		}
		zed.setLocation(new AbsoluteLocation("/" + fileName));
		zed.setExtraFields(parseExtraFields(readByteArray(ra, extraFieldsLength.intValue()), false));
		zed.setFileComment(readString(ra, fileCommentLength.intValue(), textEncodingCs, utf8Encoding));
		return shouldBeIgnored ? null : zed;
	}

	private void placeEntryInTree(ZipEntityHolder parentHolder, LinkedList<String> pathSegmentStack, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld)
	{
		int stackSize = pathSegmentStack.size();
		if (pathSegmentStack.size() == 1)
		{
			String name = pathSegmentStack.get(0);
			ZipEntityHolder zeh = parentHolder.m_childEntities.get(name);
			if (zeh == null)
			{
				zeh = new ZipEntityHolder();
				parentHolder.m_childEntities.put(name, zeh);
			}
			zeh.m_zecd = zecd;
			zeh.m_zeld = zeld;
		}
		else if (stackSize == 0)
		{
			// This may occur in Zip files, apparently.
			// "This" on the row above is a directory with no path or no name.
			// Ignore it.
		}
		else
		{
			String segmentName = pathSegmentStack.poll();
			ZipEntityHolder zeh = parentHolder.m_childEntities.get(segmentName);
			if (zeh == null)
			{
				zeh = new ZipEntityHolder();
				parentHolder.m_childEntities.put(segmentName, zeh);
			}
			placeEntryInTree(zeh, pathSegmentStack, zecd, zeld);
		}
	}

	private ZipDirectoryEntry createNonEmptyDirectoryEntry(ZipEntryCollaborator collaborator, ZipEntityHolder entityHolder, AbsoluteLocation entryLoc, Map<AbsoluteLocation, ZipEntry> entries, Charset cs)
	{
		// Create child entries first
		Map<String, ZipEntry> childEntries = new HashMap<String, ZipEntry>(entityHolder.m_childEntities.size());
		for (Map.Entry<String, ZipEntityHolder> entry : entityHolder.m_childEntities.entrySet())
		{
			childEntries.put(entry.getKey(), createEntries(collaborator, entry.getValue(), entryLoc.getChildLocation(entry.getKey()), entries, cs));
		}

		ZipEntryCentralFileHeaderData zecd = entityHolder.m_zecd;
		ZipDirectoryEntry res;
		if (zecd != null)
		{
			if (zecd.getUncompressedSize().longValue() > 0)
			{
				System.err.println("The Zip entry " + entryLoc + " has both child entries and file data. The file data will be invisible");
			}

			res = new ZipDirectoryEntry(collaborator, zecd, entityHolder.m_zeld, childEntries);
		}
		else
		{
			// This was not present as an entry in the Zip file. Create a
			// generic directory entry.
			res = new ZipDirectoryEntry(collaborator, entryLoc, childEntries);
		}
		entries.put(entryLoc, res);
		return res;
	}

	private ZipDirectoryEntry createEmptyDirectoryEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld, AbsoluteLocation entryLoc, Map<AbsoluteLocation, ZipEntry> entries)
	{
		ZipDirectoryEntry res = new ZipDirectoryEntry(collaborator, zecd, zeld, null);
		entries.put(entryLoc, res);
		return res;
	}

	private ZipFileEntry createFileEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld, AbsoluteLocation entryLoc, Map<AbsoluteLocation, ZipEntry> entries)
	{
		ZipFileEntry res = new ZipFileEntry(collaborator, zecd, zeld);
		entries.put(entryLoc, res);
		return res;
	}

	private ZipSymbolicLinkEntry createSymbolicLinkEntry(ZipEntryCollaborator collaborator, ZipEntryCentralFileHeaderData zecd, ZipEntryLocalFileHeaderData zeld, AbsoluteLocation entryLoc, Map<AbsoluteLocation, ZipEntry> entries, Charset cs)
	{
		ZipSymbolicLinkEntry res = new ZipSymbolicLinkEntry(collaborator, zecd, zeld, cs);
		entries.put(entryLoc, res);
		return res;
	}

	@SuppressWarnings("unchecked")
	private ZipEntry createEntries(ZipEntryCollaborator collaborator, ZipEntityHolder entityHolder, AbsoluteLocation entryLoc, Map<AbsoluteLocation, ZipEntry> entries, Charset cs)
	{
		if (entityHolder.m_childEntities.size() > 0)
		{
			// A non-empty directory entry
			return createNonEmptyDirectoryEntry(collaborator, entityHolder, entryLoc, entries, cs);
		}
		else
		{
			// An empty directory or a file entry

			if (entryLoc.equals(AbsoluteLocation.ROOT_DIR))
			{
				// Special case: an empty Zip file
				ZipEntry res = new ZipDirectoryEntry(collaborator, entryLoc, Collections.EMPTY_MAP);
				entries.put(AbsoluteLocation.ROOT_DIR, res);
				return res;
			}

			ZipEntryCentralFileHeaderData zecd = entityHolder.m_zecd;
			if (zecd == null)
			{
				throw new ZipFileParseException("Internal error for entry " + entryLoc);
			}

			if (!zecd.getLocation().equals(entryLoc))
			{
				throw new ZipFileParseException("Internal error " + zecd.getLocation() + " != " + entryLoc);
			}

			if (zecd.isDirectory())
			{
				return createEmptyDirectoryEntry(collaborator, zecd, entityHolder.m_zeld, entryLoc, entries);
			}
			else
			{
				if (zecd.getExternalFileAttributes() instanceof UnixExternalFileAttributes)
				{
					// Is the entry a symbolic link?
					UnixExternalFileAttributes uefa = (UnixExternalFileAttributes) zecd.getExternalFileAttributes();
					if (uefa.getEntityType() == UnixEntityType.SYMBOLIC_LINK)
					{
						return createSymbolicLinkEntry(collaborator, zecd, entityHolder.m_zeld, entryLoc, entries, cs);
					}
					else if (uefa.getEntityType() == UnixEntityType.REGULAR_FILE)
					{
						return createFileEntry(collaborator, zecd, entityHolder.m_zeld, entryLoc, entries);
					}
					else if (uefa.getEntityType() == UnixEntityType.DIRECTORY)
					{
						throw new ZipFileParseException("The unix entity type was directory, but the entry type was a file: " + zecd.getLocation());
					}
					else
					{
						System.out.println("Treating entry of type " + uefa.getEntityType() + " as a regular file: " + zecd.getLocation());
						return createFileEntry(collaborator, zecd, entityHolder.m_zeld, entryLoc, entries);
					}
				}
				else
				{
					return createFileEntry(collaborator, zecd, entityHolder.m_zeld, entryLoc, entries);
				}
			}
		}
	}

	/**
	 * Search backwards from the end of the file for the end of central
	 * directory record and read the start position of the central directory
	 * from it.
	 */
	private EndOfCentralDirectoryRecordContents parseEndOfCentralDirectoryRecord(RandomAccess ra, Charset cs)
	{
		// The start position is the file size minus the minimum size of the
		// end of central directory record plus the size of the header.
		int searchIndex = 0;
		long fileLen = ra.length();
		if (fileLen < 18)
		{
			throw new ZipFileParseException("The file was only " + fileLen + " bytes long. Is this really a Zip file?");
		}
		long curPos = fileLen - 18;
		ra.seek(curPos);
		EndOfCentralDirectoryRecordContents res = new EndOfCentralDirectoryRecordContents();
		boolean found = false;
		while (!found)
		{
			if (ra.read() == END_OF_CENTRAL_DIRECTORY_HEADER_BACKWARDS[searchIndex])
			{
				searchIndex++;
				if (searchIndex == 4)
				{
					// found
					ra.skipBytes(15);
					res.m_startOfCentralDirectory = UnsignedInteger.readBigEndian(ra);
					found = true;
				}
			}
			else
			{
				searchIndex = 0;
			}
			if (!found)
			{
				curPos--;
				if (curPos < 0)
				{
					throw new ZipFileParseException("Could not find the end of central directory header. Is this really a Zip file?");
				}
				else if (fileLen - curPos > BYTES_TO_SCAN_FOR_END_OF_CENTRAL_DIRECTORY_HEADER_BEFORE_GIVING_UP)
				{
					throw new ZipFileParseException("Could not find the end of central directory header. Is this really a Zip file? (If it is, report this as a bug!)");
				}
				ra.seek(curPos);
			}
		}

		int commentLength = UnsignedShort.readBigEndian(ra).intValue();
		if (commentLength > 0)
		{
			res.m_fileComment = readString(ra, commentLength, cs, false);
		}
		else
		{
			res.m_fileComment = "";
		}
		return res;
	}

	private ZipEntryLocalFileHeaderData readLocalFileHeader(RandomAccess ra)
	{
		ZipEntryLocalFileHeaderData res = new ZipEntryLocalFileHeaderData();
		ra.skipBytes(26);
		UnsignedShort fileNameLength = UnsignedShort.readBigEndian(ra);
		UnsignedShort extraFieldsLength = UnsignedShort.readBigEndian(ra);
		ra.skipBytes(fileNameLength.intValue());
		res.setExtraFields(parseExtraFields(readByteArray(ra, extraFieldsLength.intValue()), true));
		res.setStartPositionOfFileData(ra.getFilePointer());
		return res;
	}

	/**
	 * Parse the Zip file.
	 * @param collaborator Collaborator object that is used to access the file
	 * that the {@link ZipFile} object is created from.
	 * @param ra A random access on the Zip file.
	 * @param fileNameEncodingCs The charset used to decode file names.
	 * @param textEncodingCs The charset used to decode other text entries, such
	 * as Zip entry comments.
	 * @return The contents of the Zip file. This is used by the {@link ZipFile}
	 * object to initialize itself.
	 * @throws ZipFileParseException If the Zip file cannot be parsed for some
	 * reason other than an I/O error.
	 * @throws WrappedIOException On I/O errors.
	 */
	public ZipFileContents parse(ZipEntryCollaborator collaborator, RandomAccess ra, Charset fileNameEncodingCs, Charset textEncodingCs) throws ZipFileParseException, WrappedIOException
	{
		try
		{
			// Go to the start of the central directory
			EndOfCentralDirectoryRecordContents eocd = parseEndOfCentralDirectoryRecord(ra, textEncodingCs);
			ra.seek(eocd.m_startOfCentralDirectory.longValue());

			UnsignedInteger header = UnsignedInteger.readBigEndian(ra);
			byte[] headerBarr = new byte[UnsignedInteger.SIZE];

			List<ZipEntryCentralFileHeaderData> centralFileHeaders = new ArrayList<ZipEntryCentralFileHeaderData>();

			// Read central file headers.
			int noFiles = 0;
			while (isCentralFileHeader(header))
			{
				ZipEntryCentralFileHeaderData cfhd = readCentralFileHeader(ra, fileNameEncodingCs, textEncodingCs);
				if (cfhd != null)
				{
					noFiles++;
					centralFileHeaders.add(cfhd);
				}

				int noRead = ra.read(headerBarr);
				if (noRead != UnsignedInteger.SIZE)
				{
					header = null;
				}
				else
				{
					header = UnsignedInteger.fromBigEndianByteArray(headerBarr);
				}
			}

			ZipEntityHolder rootDirectoryHolder = new ZipEntityHolder();

			// Read local file headers
			for (ZipEntryCentralFileHeaderData zecd : centralFileHeaders)
			{
				ra.seek(zecd.getRelativeOffsetOfLocalHeader().longValue());
				ZipEntryLocalFileHeaderData zeld = readLocalFileHeader(ra);

				placeEntryInTree(rootDirectoryHolder, zecd.getLocation().getPathSegmentStack(), zecd, zeld);
			}

			Map<AbsoluteLocation, ZipEntry> entryMap = new HashMap<AbsoluteLocation, ZipEntry>(noFiles);
			ZipEntry rootEntry = createEntries(collaborator, rootDirectoryHolder, AbsoluteLocation.ROOT_DIR, entryMap, textEncodingCs);
			return new ZipFileContents((ZipDirectoryEntry) rootEntry, entryMap, eocd.m_fileComment);
		}
		catch (RuntimeException e)
		{
			throw new ZipFileParseException("At position " + ra.getFilePointer(), e);
		}
	}
}
