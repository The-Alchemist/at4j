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
package org.at4j.zip.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.at4j.archive.builder.AbstractStreamAddCapableArchiveBuilder;
import org.at4j.archive.builder.ArchiveBuilder;
import org.at4j.archive.builder.ArchiveEntryAddException;
import org.at4j.support.lang.UnsignedInteger;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.support.nio.charset.Charsets;
import org.at4j.support.util.MsDosDate;
import org.at4j.support.util.MsDosTime;
import org.at4j.zip.ZipGeneralPurposeBitFlags;
import org.at4j.zip.ZipInternalFileAttributes;
import org.at4j.zip.comp.StoredCompressionMethod;
import org.at4j.zip.comp.ZipEntryCompressionMethodFactory;
import org.at4j.zip.ef.ZipEntryExtraField;
import org.at4j.zip.ef.ZipEntryExtraFieldFactory;
import org.at4j.zip.extattrs.UnixEntityType;
import org.at4j.zip.extattrs.ZipExternalFileAttributes;
import org.at4j.zip.extattrs.ZipExternalFileAttributesFactory;
import org.entityfs.RandomAccess;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.lock.DummyLock;
import org.entityfs.lock.ReadLockRequiredException;
import org.entityfs.lock.ReadLockable;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.RandomAccessMode;

/**
 * The Zip builder is an {@link org.at4j.archive.builder.ArchiveBuilder} for
 * building Zip files. Files and directories are written to the archive as they
 * are added using any of this object's {@code add} methods.
 * <p>
 * The Zip file format gives great flexibility in the kind of metadata that is
 * stored along with the Zip entries. All Zip entries have a set of metadata
 * properties in common, such as the last modification time of the file system
 * entity (file or directory) that was used to create the entry or the
 * <i>internal file attributes</i> (
 * {@link org.at4j.zip.ZipInternalFileAttributes}). In addition to the generic
 * metadata, <i>External file attributes</i> (used for both files and
 * directories) store metadata that is specific for the platform where the Zip
 * archive was created (Windows, Unix, etc). <i>Extra fields</i> can be used to
 * store additional metadata. Files added to the Zip archive may be compressed
 * using one of several compression methods.
 * <p>
 * The Zip builder uses a {@link ZipExternalFileAttributesFactory} to create
 * external file attributes for added files and directories. It uses zero or
 * more {@link ZipEntryExtraFieldFactory}:s to create extra fields for entries,
 * and it uses a {@link ZipEntryCompressionMethodFactory} to compress file
 * entries. To know which implementation of each factory to use for a given
 * entry, which means to know the effective {@link ZipEntrySettings} for an
 * entry, it uses default settings, global rules and entry-specific rules in the
 * way described in the {@link ArchiveBuilder} documentation.
 * <p>
 * The Zip builder uses a {@link InternalFileAttributesStrategy} to determine
 * the internal file attributes for each entry. The default strategy,
 * {@link DefaultInternalFileAttributesStrategy}, can be exchanged for a custom
 * strategy if necessary.
 * <p>
 * Global Zip file settings, such as the Zip file comment, are set directly on
 * this object.
 * <p>
 * This implementation does not support adding symbolic links to the archive.
 * <p>
 * If it is in a locking {@link org.entityfs.FileSystem}, the target file is
 * locked for writing until the Zip builder is {@link #close()}:d.
 * <p>
 * This object is <i>not</i> safe to use concurrently from several threads
 * without external synchronization.
 * @author Karl Gustafsson
 * @since 1.0
 * @see org.at4j.zip.ZipFile
 */
public class ZipBuilder extends AbstractStreamAddCapableArchiveBuilder<ZipBuilder, ZipEntrySettings> implements ZipBuilderConfiguration
{
	private static class ExtraFieldsCreationResult
	{
		private List<byte[]> m_localHeaderExtraFieldFragments;
		private List<byte[]> m_centralHeaderExtraFieldFragments;
		private UnsignedShort m_localHeaderExtraFieldSize;
		private UnsignedShort m_centralHeaderExtraFieldSize;
	}

	private static final int LOCAL_HEADER_SIZE = 30;
	private static final int CENTRAL_HEADER_SIZE = 46;
	// The size except for the Zip file comment size which is variable.
	private static final int END_OF_CENTRAL_DIRECTORY_RECORD_SIZE = 22;

	private static final byte[] LOCAL_FILE_HEADER_SIGNATURE = new byte[] { (byte) 4, (byte) 3, (byte) 0x4b, (byte) 0x50 };
	private static final byte[] CENTRAL_FILE_HEADER_SIGNATURE = new byte[] { (byte) 2, (byte) 1, (byte) 0x4b, (byte) 0x50 };
	private static final byte[] END_OF_CENTRAL_DIRECTORY_SIGNATURE = new byte[] { (byte) 6, (byte) 5, (byte) 0x4b, (byte) 0x50 };
	private static final byte[] ZERO_BYTES = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

	private final Lock m_targetWriteLock;
	private final RandomAccess m_targetRandomAccess;
	private final boolean m_closeRandomAccessWhenClosingBuilder;
	private final Charset m_fileNameEncodingCharset;
	private final Charset m_textEncodingCharset;
	// The central file header is written to this stream as entities are added
	// to the Zip archive. When the builder is closed, the contents of this
	// stream is written at the end of the created Zip file.
	private final ByteArrayOutputStream m_centralHeaderStream;

	private InternalFileAttributesStrategy m_internalFileAttributesStrategy;
	// The file comment. This may be null
	private final String m_fileComment;
	private int m_numberOfEntriesInCentralDirectory;

	// This is set to true by the close() method.
	private boolean m_closed;

	/**
	 * Create a new Zip builder that uses the default default entry settings
	 * when adding entries, and the platform's default charset to encode entry
	 * names and other text data.
	 * @param target The target file. The previous contents of this file is
	 * discarded. If it is in a locking {@link org.entityfs.FileSystem}, the
	 * file will be locked for writing by the calling thread until this object
	 * is {@link #close()}:d.
	 * @throws WrappedIOException On I/O errors
	 * @see #ZipBuilder(RandomlyAccessibleFile, ZipBuilderSettings)
	 * @see #ZipBuilder(RandomAccess, ZipBuilderSettings)
	 */
	public ZipBuilder(RandomlyAccessibleFile target) throws WrappedIOException
	{
		this(target, null);
	}

	/**
	 * Create a new Zip builder that uses the supplied default file and
	 * directory entry settings when adding entries, and the supplied charsets
	 * to encode entry names and other text metadata.
	 * @param target The target file. The previous contents of this file is
	 * discarded. If it is in a locking {@link org.entityfs.FileSystem}, the
	 * file will be locked for writing by the calling thread until this object
	 * is {@link #close()}:d.
	 * @param settings The configuration of the Zip builder. Set this to {@code
	 * null} to use the default configuration.
	 * @throws WrappedIOException On I/O errors
	 * @see #ZipBuilder(RandomlyAccessibleFile)
	 * @see #ZipBuilder(RandomAccess, ZipBuilderSettings)
	 */
	public ZipBuilder(RandomlyAccessibleFile target, ZipBuilderSettings settings) throws WrappedIOException
	{
		super(settings != null ? ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_FILE_ENTRY_SETTINGS.combineWith(settings.getDefaultFileEntrySettings()) : ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_FILE_ENTRY_SETTINGS,
				settings != null ? ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_DIRECTORY_ENTRY_SETTINGS.combineWith(settings.getDefaultDirectoryEntrySettings()) : ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_DIRECTORY_ENTRY_SETTINGS);

		// Null checks
		target.getClass();

		m_fileNameEncodingCharset = settings != null ? settings.getFileNameEncodingCharset() : ZipBuilderSettings.DEFAULT_FILE_NAME_ENCODING_CHARSET;
		m_textEncodingCharset = settings != null ? settings.getTextEncodingCharset() : ZipBuilderSettings.DEFAULT_TEXT_ENCODING_CHARSET;
		m_centralHeaderStream = new ByteArrayOutputStream();
		m_closeRandomAccessWhenClosingBuilder = true;
		m_internalFileAttributesStrategy = settings != null ? settings.getInternalFileAttributesStrategy() : new DefaultInternalFileAttributesStrategy();
		m_fileComment = settings != null ? settings.getFileComment() : ZipBuilderSettings.DEFAULT_FILE_COMMENT;

		boolean successful = false;
		RandomAccess targetRandomAccess = null;
		m_targetWriteLock = target.lockForWriting();
		try
		{
			targetRandomAccess = target.openForRandomAccess(RandomAccessMode.READ_WRITE);
			targetRandomAccess.setLength(0);
			m_targetRandomAccess = targetRandomAccess;
			successful = true;
		}
		finally
		{
			if (!successful)
			{
				m_closed = true;
				m_targetWriteLock.unlock();
				if (targetRandomAccess != null)
				{
					targetRandomAccess.close();
				}
			}
		}
	}

	/**
	 * Create a Zip builder on an already open {@link org.entityfs.RandomAccess}
	 * object. The builder will use the supplied charsets for encoding text and
	 * the supplied default entry settings.
	 * @param target The target {@code RandomAccess}. It is <i>not</i> closed by
	 * this object when {@link #close()} is called. This object assumes that it
	 * has exclusive access to the {@code RandomAccess}.
	 * @param settings The configuration of the Zip builder. Set this to {@code
	 * null} to use the default configuration.
	 * @throws WrappedIOException On I/O errors
	 * @see #ZipBuilder(RandomlyAccessibleFile)
	 * @see #ZipBuilder(RandomlyAccessibleFile, ZipBuilderSettings)
	 */
	public ZipBuilder(RandomAccess target, ZipBuilderSettings settings) throws WrappedIOException
	{
		super(settings != null ? ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_FILE_ENTRY_SETTINGS.combineWith(settings.getDefaultFileEntrySettings()) : ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_FILE_ENTRY_SETTINGS,
				settings != null ? ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_DIRECTORY_ENTRY_SETTINGS.combineWith(settings.getDefaultDirectoryEntrySettings()) : ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_DIRECTORY_ENTRY_SETTINGS);

		// Null check
		target.getClass();

		m_fileNameEncodingCharset = settings != null ? settings.getFileNameEncodingCharset() : ZipBuilderSettings.DEFAULT_FILE_NAME_ENCODING_CHARSET;
		m_textEncodingCharset = settings != null ? settings.getTextEncodingCharset() : ZipBuilderSettings.DEFAULT_TEXT_ENCODING_CHARSET;
		m_centralHeaderStream = new ByteArrayOutputStream();
		m_closeRandomAccessWhenClosingBuilder = false;
		m_targetRandomAccess = target;
		m_targetWriteLock = DummyLock.INSTANCE;
		m_internalFileAttributesStrategy = settings != null ? settings.getInternalFileAttributesStrategy() : new DefaultInternalFileAttributesStrategy();
		m_fileComment = settings != null ? settings.getFileComment() : ZipBuilderSettings.DEFAULT_FILE_COMMENT;
	}

	/**
	 * Set the strategy to use for determining a Zip entry's internal file
	 * attributes property.
	 * <p>
	 * By default, the Zip builder uses an instance of the
	 * {@link DefaultInternalFileAttributesStrategy}.
	 * @param strat The new internal file attributes strategy.
	 * @return {@code this}
	 * @see #getInternalFileAttributesStrategy()
	 */
	public ZipBuilder setInternalFileAttributesStrategy(InternalFileAttributesStrategy strat)
	{
		// Null check
		strat.getClass();
		m_internalFileAttributesStrategy = strat;
		return this;
	}

	/**
	 * Get the strategy used for determining a Zip entry's internal file
	 * attributes property.
	 * @return The strategy.
	 * @see #setInternalFileAttributesStrategy(InternalFileAttributesStrategy)
	 */
	public InternalFileAttributesStrategy getInternalFileAttributesStrategy()
	{
		return m_internalFileAttributesStrategy;
	}

	/**
	 * This method returns the default directory entry settings.
	 * <p>
	 * The returned object should not be modified in any way by the caller.
	 * @return The default directory entry settings.
	 */
	@Override
	protected ZipEntrySettings getDefaultDefaultDirectoryEntrySettings()
	{
		return ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_DIRECTORY_ENTRY_SETTINGS;
	}

	/**
	 * This method returns the default file entry settings.
	 * <p>
	 * The returned object should not be modified in any way by the caller.
	 * @return The default file entry settings.
	 */
	@Override
	protected ZipEntrySettings getDefaultDefaultFileEntrySettings()
	{
		return ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_FILE_ENTRY_SETTINGS;
	}

	/**
	 * Get the Zip file comment.
	 * @return The Zip file comment. This may be {@code null}.
	 */
	public String getFileComment()
	{
		return m_fileComment;
	}

	/**
	 * Get the charset used for encoding file names in the Zip file.
	 * @return The charset used for encoding file names in the Zip file.
	 * @see #getTextEncodingCharset()
	 */
	public Charset getFileNameEncodingCharset()
	{
		return m_fileNameEncodingCharset;
	}

	/**
	 * Get the charset used for encoding text such as comments in the Zip file.
	 * @return The charset used for encoding text in the Zip file.
	 * @see #getFileNameEncodingCharset()
	 */
	public Charset getTextEncodingCharset()
	{
		return m_textEncodingCharset;
	}

	/**
	 * This method throws an {@link IllegalStateException} if the Zip builder is
	 * closed.
	 * @throws IllegalStateException If the Zip builder is closed.
	 */
	@Override
	protected void assertNotClosed() throws IllegalStateException
	{
		if (m_closed)
		{
			throw new IllegalStateException("This ZipBuilder is closed");
		}
	}

	/**
	 * This method throws a {@link ReadLockRequiredException} if the supplied
	 * object is not locked for reading.
	 * @param o The object to check for locking.
	 * @throws ReadLockRequiredException If the supplied object is not locked
	 * for reading by the calling thread.
	 */
	protected void assertReadLocked(ReadLockable o) throws ReadLockRequiredException
	{
		if (!o.isReadLockedByCurrentThread())
		{
			throw new ReadLockRequiredException(o + " must be locked for reading when calling this method.");
		}
	}

	private byte[] createGeneralPurposeBitFlags(ZipEntrySettings effectiveSettings)
	{
		ZipGeneralPurposeBitFlags res = new ZipGeneralPurposeBitFlags();
		// Nothing here for now...
		return res.toBytes(effectiveSettings.getCompressionMethod());
	}

	/**
	 * Create local and central header versions of the extra fields.
	 * @returnThe local and central header versions of the extra fields in a
	 * byte[2][][]. Each extra field (local and central) is returned as an array
	 * of data fragments.
	 */
	private ExtraFieldsCreationResult createExtraFields(AbsoluteLocation loc, UnixEntityType entityType, Object entryToZip, ZipEntrySettings effectiveSettings)
	{
		ExtraFieldsCreationResult res = new ExtraFieldsCreationResult();
		int centralHeaderExtraFieldSize = 0;
		int localHeaderExtraFieldSize = 0;
		res.m_centralHeaderExtraFieldFragments = new ArrayList<byte[]>();
		res.m_localHeaderExtraFieldFragments = new ArrayList<byte[]>();

		for (ZipEntryExtraFieldFactory f : effectiveSettings.getExtraFieldFactories())
		{
			byte[] efcode = f.getCode().getBigEndianByteArray();
			{
				ZipEntryExtraField lef = f.create(true, loc, entityType, entryToZip, effectiveSettings);
				byte[] lefb = lef.encode(this);
				byte[] lefbs = UnsignedShort.valueOf(lefb.length).getBigEndianByteArray();
				byte[] lheader = new byte[4];
				lheader[0] = efcode[0];
				lheader[1] = efcode[1];
				lheader[2] = lefbs[0];
				lheader[3] = lefbs[1];
				res.m_localHeaderExtraFieldFragments.add(lheader);
				if (lefb.length > 0)
				{
					res.m_localHeaderExtraFieldFragments.add(lefb);
				}
				localHeaderExtraFieldSize += 4 + lefb.length;
			}

			{
				ZipEntryExtraField cef = f.create(false, loc, entityType, entryToZip, effectiveSettings);
				byte[] cefb = cef.encode(this);
				byte[] cefbs = UnsignedShort.valueOf(cefb.length).getBigEndianByteArray();
				byte[] cheader = new byte[4];
				cheader[0] = efcode[0];
				cheader[1] = efcode[1];
				cheader[2] = cefbs[0];
				cheader[3] = cefbs[1];
				res.m_centralHeaderExtraFieldFragments.add(cheader);
				if (cefb.length > 0)
				{
					res.m_centralHeaderExtraFieldFragments.add(cefb);
				}
				centralHeaderExtraFieldSize += 4 + cefb.length;
			}
		}

		res.m_centralHeaderExtraFieldSize = UnsignedShort.valueOf(centralHeaderExtraFieldSize);
		res.m_localHeaderExtraFieldSize = UnsignedShort.valueOf(localHeaderExtraFieldSize);
		return res;
	}

	/**
	 * Create local and central file headers. Some values are not filled in yet
	 * because they require that the file has been compressed first.
	 * @param fwr If {@code null}, it is assumed that the entry is a directory.
	 * @return The local and central file headers (in a byte[2][])
	 */
	private byte[][] createFileHeaders(ZipEntrySettings effectiveSettings, Date lastModified, FileWriteResult fwr, UnsignedShort fileNameLength, UnsignedShort fileCommentLength, UnsignedShort localExtraFieldsLength,
			UnsignedShort centralExtraFieldsLength, ZipInternalFileAttributes internalFileAttributes, UnsignedInteger relativeOffsetOfLocalHeader, ZipExternalFileAttributes extFileAttrs)
	{
		byte[] localHeader = new byte[LOCAL_HEADER_SIZE];
		byte[] centralHeader = new byte[CENTRAL_HEADER_SIZE];

		// Local header signature, big-endian
		localHeader[0] = LOCAL_FILE_HEADER_SIGNATURE[3];
		localHeader[1] = LOCAL_FILE_HEADER_SIGNATURE[2];
		localHeader[2] = LOCAL_FILE_HEADER_SIGNATURE[1];
		localHeader[3] = LOCAL_FILE_HEADER_SIGNATURE[0];

		// Central header signature, big-endian
		centralHeader[0] = CENTRAL_FILE_HEADER_SIGNATURE[3];
		centralHeader[1] = CENTRAL_FILE_HEADER_SIGNATURE[2];
		centralHeader[2] = CENTRAL_FILE_HEADER_SIGNATURE[1];
		centralHeader[3] = CENTRAL_FILE_HEADER_SIGNATURE[0];

		// Use the version required to extract for the version used to create.
		byte[] versionNeededToExtract = fwr != null ? fwr.getCompressionMethod().getVersionNeededToExtract().getBigEndianByteArray() : new byte[] { (byte) 10, (byte) 0 };
		centralHeader[4] = versionNeededToExtract[0];

		// The version made by code
		centralHeader[5] = extFileAttrs.getVersionMadeBy().getCode().byteValue();

		// The version needed to extract
		localHeader[4] = versionNeededToExtract[0];
		localHeader[5] = versionNeededToExtract[1];
		centralHeader[6] = versionNeededToExtract[0];
		centralHeader[7] = versionNeededToExtract[1];

		// General purpose bit flag
		byte[] gpBitFlag = createGeneralPurposeBitFlags(effectiveSettings);
		localHeader[6] = gpBitFlag[0];
		localHeader[7] = gpBitFlag[1];
		centralHeader[8] = gpBitFlag[0];
		centralHeader[9] = gpBitFlag[1];

		// Compression method
		byte[] compressionMethod = fwr != null ? fwr.getCompressionMethod().getCode().getBigEndianByteArray() : StoredCompressionMethod.CODE.getBigEndianByteArray();
		localHeader[8] = compressionMethod[0];
		localHeader[9] = compressionMethod[1];
		centralHeader[10] = compressionMethod[0];
		centralHeader[11] = compressionMethod[1];

		// Last mod file time
		Calendar lastMod = Calendar.getInstance();
		lastMod.clear();
		lastMod.setTime(lastModified);
		byte[] lastModFileTime = MsDosTime.encodeMsDosTime(lastMod).getBigEndianByteArray();
		localHeader[10] = lastModFileTime[0];
		localHeader[11] = lastModFileTime[1];
		centralHeader[12] = lastModFileTime[0];
		centralHeader[13] = lastModFileTime[1];

		// Last mod file date
		byte[] lastModFileDate = MsDosDate.encodeMsDosDate(lastMod).getBigEndianByteArray();
		localHeader[12] = lastModFileDate[0];
		localHeader[13] = lastModFileDate[1];
		centralHeader[14] = lastModFileDate[0];
		centralHeader[15] = lastModFileDate[1];

		// CRC32
		byte[] crc32Checksum = fwr != null ? UnsignedInteger.valueOf(fwr.getCrc32Checksum()).getBigEndianByteArray() : ZERO_BYTES;
		localHeader[14] = crc32Checksum[0];
		localHeader[15] = crc32Checksum[1];
		localHeader[16] = crc32Checksum[2];
		localHeader[17] = crc32Checksum[3];
		centralHeader[16] = crc32Checksum[0];
		centralHeader[17] = crc32Checksum[1];
		centralHeader[18] = crc32Checksum[2];
		centralHeader[19] = crc32Checksum[3];

		// Compressed size
		byte[] compressedSize = fwr != null ? UnsignedInteger.valueOf(fwr.getCompressedSize()).getBigEndianByteArray() : ZERO_BYTES;
		localHeader[18] = compressedSize[0];
		localHeader[19] = compressedSize[1];
		localHeader[20] = compressedSize[2];
		localHeader[21] = compressedSize[3];
		centralHeader[20] = compressedSize[0];
		centralHeader[21] = compressedSize[1];
		centralHeader[22] = compressedSize[2];
		centralHeader[23] = compressedSize[3];

		// Uncompressed size
		byte[] uncompressedSize = fwr != null ? UnsignedInteger.valueOf(fwr.getUncompressedSize()).getBigEndianByteArray() : ZERO_BYTES;
		localHeader[22] = uncompressedSize[0];
		localHeader[23] = uncompressedSize[1];
		localHeader[24] = uncompressedSize[2];
		localHeader[25] = uncompressedSize[3];
		centralHeader[24] = uncompressedSize[0];
		centralHeader[25] = uncompressedSize[1];
		centralHeader[26] = uncompressedSize[2];
		centralHeader[27] = uncompressedSize[3];

		// File name length
		byte[] flarr = fileNameLength.getBigEndianByteArray();
		localHeader[26] = flarr[0];
		localHeader[27] = flarr[1];
		centralHeader[28] = flarr[0];
		centralHeader[29] = flarr[1];

		// Extra fields length
		byte[] lefl = localExtraFieldsLength.getBigEndianByteArray();
		localHeader[28] = lefl[0];
		localHeader[29] = lefl[1];
		byte[] cefl = centralExtraFieldsLength.getBigEndianByteArray();
		centralHeader[30] = cefl[0];
		centralHeader[31] = cefl[1];

		// File comment length
		byte[] fcl = fileCommentLength.getBigEndianByteArray();
		centralHeader[32] = fcl[0];
		centralHeader[33] = fcl[1];

		// Disk number start
		centralHeader[34] = (byte) 0;
		centralHeader[35] = (byte) 0;

		// Internal file attributes
		byte[] ifa = internalFileAttributes.getEncodedValue().getBigEndianByteArray();
		centralHeader[36] = ifa[0];
		centralHeader[37] = ifa[1];

		// External file attributes
		byte[] efa = extFileAttrs.getEncodedValue().getBigEndianByteArray();
		centralHeader[38] = efa[0];
		centralHeader[39] = efa[1];
		centralHeader[40] = efa[2];
		centralHeader[41] = efa[3];

		// Relative offset of local header
		byte[] roolh = relativeOffsetOfLocalHeader.getBigEndianByteArray();
		centralHeader[42] = roolh[0];
		centralHeader[43] = roolh[1];
		centralHeader[44] = roolh[2];
		centralHeader[45] = roolh[3];

		return new byte[][] { localHeader, centralHeader };
	}

	/**
	 * Write a file entry to the Zip file. This method writes the entry to the
	 * file and saves the entry's central directory record for later writing.
	 * @param loc The absolute location for the entry in the file.
	 * @param f The file to write. The file should be locked for reading when
	 * this method is called.
	 * @param effectiveSettings The effective Zip entry settings for this entry.
	 * @throws IOException On I/O errors
	 */
	private void writeFile(AbsoluteLocation loc, FileWriteStrategy fws, Object entryToZip, ZipEntrySettings effectiveSettings, Date lastModified) throws IOException
	{
		byte[] fileName = loc.getLocation().substring(1).getBytes(m_fileNameEncodingCharset.name());

		// Create extra fields
		ExtraFieldsCreationResult extraFields = createExtraFields(loc, UnixEntityType.REGULAR_FILE, entryToZip, effectiveSettings);

		byte[][] headers;
		byte[] fileComment;

		// Write the file name, the local extra fields and the file, but leave 
		// the local header blank so far
		long startPos = m_targetRandomAccess.getFilePointer();
		boolean successful = false;
		try
		{
			m_targetRandomAccess.seek(startPos + LOCAL_HEADER_SIZE);

			m_targetRandomAccess.write(fileName);

			for (byte[] localExtraFieldFragment : extraFields.m_localHeaderExtraFieldFragments)
			{
				m_targetRandomAccess.write(localExtraFieldFragment);
			}

			FileWriteResult fwr = fws.writeFile(m_targetRandomAccess);

			String fc = effectiveSettings.getComment();
			fileComment = fc != null ? fc.getBytes(m_textEncodingCharset.name()) : new byte[0];

			ZipInternalFileAttributes intFileAttributes = m_internalFileAttributesStrategy.createInternalFileAttributes(UnixEntityType.REGULAR_FILE, loc);

			ZipExternalFileAttributes extFileAttrs = effectiveSettings.getExternalFileAttributesFactory().create(UnixEntityType.REGULAR_FILE, loc, entryToZip);

			headers = createFileHeaders(effectiveSettings, lastModified, fwr, UnsignedShort.valueOf(fileName.length), UnsignedShort.valueOf(fileComment.length), extraFields.m_localHeaderExtraFieldSize,
					extraFields.m_centralHeaderExtraFieldSize, intFileAttributes, UnsignedInteger.valueOf(startPos), extFileAttrs);

			long endPos = m_targetRandomAccess.getFilePointer();

			// Rewind to write the local file header.
			m_targetRandomAccess.seek(startPos);
			m_targetRandomAccess.write(headers[0]);
			assert m_targetRandomAccess.getFilePointer() == startPos + LOCAL_HEADER_SIZE;
			m_targetRandomAccess.seek(endPos);
			successful = true;
		}
		finally
		{
			if (!successful)
			{
				// Delete the unsuccessful entry
				m_targetRandomAccess.setLength(startPos);
				m_targetRandomAccess.seek(startPos);
			}
		}

		// Write the central file header
		m_centralHeaderStream.write(headers[1]);
		m_centralHeaderStream.write(fileName);
		for (byte[] centralExtraFieldFragment : extraFields.m_centralHeaderExtraFieldFragments)
		{
			m_centralHeaderStream.write(centralExtraFieldFragment);
		}
		m_centralHeaderStream.write(fileComment);
		m_numberOfEntriesInCentralDirectory++;
	}

	/**
	 * Write a single directory entry to the Zip file. The directory's contents
	 * is not added.
	 * @param loc The absolute location for the entry in the file.
	 * @param effectiveSettings The effective Zip entry settings for this entry.
	 * @throws IOException On I/O errors
	 */
	private void writeDirectory(AbsoluteLocation loc, Object entryToZip, ZipEntrySettings effectiveSettings, Date lastModified) throws IOException
	{
		byte[] fileName = (loc.getLocation() + "/").substring(1).getBytes(m_fileNameEncodingCharset.name());

		// Create extra fields
		ExtraFieldsCreationResult extraFields = createExtraFields(loc, UnixEntityType.DIRECTORY, entryToZip, effectiveSettings);

		byte[][] headers;
		byte[] fileComment;

		long startPos = m_targetRandomAccess.getFilePointer();
		boolean successful = false;
		try
		{
			ZipInternalFileAttributes intFileAttributes = m_internalFileAttributesStrategy.createInternalFileAttributes(UnixEntityType.DIRECTORY, loc);

			ZipExternalFileAttributes extFileAttrs = effectiveSettings.getExternalFileAttributesFactory().create(UnixEntityType.DIRECTORY, loc, entryToZip);

			String fc = effectiveSettings.getComment();
			fileComment = fc != null ? fc.getBytes(m_textEncodingCharset.name()) : new byte[0];

			// Create local and central file headers
			headers = createFileHeaders(effectiveSettings, lastModified, null, UnsignedShort.valueOf(fileName.length), UnsignedShort.valueOf(fileComment.length), extraFields.m_localHeaderExtraFieldSize,
					extraFields.m_centralHeaderExtraFieldSize, intFileAttributes, UnsignedInteger.valueOf(startPos), extFileAttrs);

			// Local file header
			m_targetRandomAccess.write(headers[0]);
			// Directory name
			m_targetRandomAccess.write(fileName);
			// Local extra fields
			for (byte[] localExtraFieldFragment : extraFields.m_localHeaderExtraFieldFragments)
			{
				m_targetRandomAccess.write(localExtraFieldFragment);
			}
			successful = true;
		}
		finally
		{
			if (!successful)
			{
				// Delete the unsuccessful entry
				m_targetRandomAccess.setLength(startPos);
				m_targetRandomAccess.seek(startPos);
			}
		}

		// Write the central file header
		m_centralHeaderStream.write(headers[1]);
		m_centralHeaderStream.write(fileName);
		for (byte[] centralExtraFieldFragment : extraFields.m_centralHeaderExtraFieldFragments)
		{
			m_centralHeaderStream.write(centralExtraFieldFragment);
		}
		m_centralHeaderStream.write(fileComment);
		m_numberOfEntriesInCentralDirectory++;
	}

	@Override
	protected void addDirectoryCallback(AbsoluteLocation location, Object d, ZipEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		try
		{
			writeDirectory(location, d, effectiveSettings, lastModified);
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	@Override
	protected void addFileCallback(AbsoluteLocation location, ReadableFile f, ZipEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		try
		{
			writeFile(location, new RereadableFileWriteStrategy(f, effectiveSettings), f, effectiveSettings, lastModified);
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	@Override
	protected void addStreamCallback(AbsoluteLocation location, InputStream is, ZipEntrySettings effectiveSettings, Date lastModified) throws WrappedIOException, ArchiveEntryAddException
	{
		try
		{
			writeFile(location, new StreamFileWriteStrategy(is, effectiveSettings), is, effectiveSettings, lastModified);
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	public boolean isClosed()
	{
		return m_closed;
	}

	private byte[] createEndOfCentralDirectoryRecord(UnsignedInteger startOfCentralDirectory, UnsignedInteger sizeOfCentralDirectory)
	{
		byte[] fileComment = m_fileComment != null ? Charsets.getBytes(m_fileComment, m_textEncodingCharset) : new byte[0];

		byte[] res = new byte[END_OF_CENTRAL_DIRECTORY_RECORD_SIZE + fileComment.length];

		// End of central directory signature, big endian
		res[0] = END_OF_CENTRAL_DIRECTORY_SIGNATURE[3];
		res[1] = END_OF_CENTRAL_DIRECTORY_SIGNATURE[2];
		res[2] = END_OF_CENTRAL_DIRECTORY_SIGNATURE[1];
		res[3] = END_OF_CENTRAL_DIRECTORY_SIGNATURE[0];

		// Number of this disk
		res[4] = 0;
		res[5] = 0;

		// Number of the disk with the start of the central directory
		res[6] = 0;
		res[7] = 0;

		// Total number of entries in the central directory on this disk, big
		// endian
		byte[] numberOfEntries = UnsignedShort.valueOf(m_numberOfEntriesInCentralDirectory).getBigEndianByteArray();
		res[8] = numberOfEntries[0];
		res[9] = numberOfEntries[1];

		// Total number of entries in the central directory, big endian
		res[10] = numberOfEntries[0];
		res[11] = numberOfEntries[1];

		// Size of the central directory, big endian
		byte[] siocd = sizeOfCentralDirectory.getBigEndianByteArray();
		res[12] = siocd[0];
		res[13] = siocd[1];
		res[14] = siocd[2];
		res[15] = siocd[3];

		// Offset of start of central directory with respect to the starting
		// disk number, big endian
		byte[] socd = startOfCentralDirectory.getBigEndianByteArray();
		res[16] = socd[0];
		res[17] = socd[1];
		res[18] = socd[2];
		res[19] = socd[3];

		// Zip file comment length, big endian
		byte[] fileCommentLength = UnsignedShort.valueOf(fileComment.length).getBigEndianByteArray();
		res[20] = fileCommentLength[0];
		res[21] = fileCommentLength[1];

		// Zip file comment
		if (fileComment.length > 0)
		{
			System.arraycopy(fileComment, 0, res, 22, fileComment.length);
		}

		return res;
	}

	/**
	 * Finish the Zip file and close the Zip builder.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void close() throws WrappedIOException
	{
		if (!m_closed)
		{
			try
			{
				try
				{
					// Write the central directory
					try
					{
						m_centralHeaderStream.close();
					}
					catch (IOException e)
					{
						throw new WrappedIOException(e);
					}

					long startOfCentralDirectory = m_targetRandomAccess.getFilePointer();
					m_targetRandomAccess.write(m_centralHeaderStream.toByteArray());
					long sizeOfCentralDirectory = m_targetRandomAccess.getFilePointer() - startOfCentralDirectory;

					// Write the end of central directory record
					m_targetRandomAccess.write(createEndOfCentralDirectoryRecord(UnsignedInteger.valueOf(startOfCentralDirectory), UnsignedInteger.valueOf(sizeOfCentralDirectory)));
				}
				finally
				{
					if (m_closeRandomAccessWhenClosingBuilder)
					{
						m_targetRandomAccess.close();
					}
				}
			}
			finally
			{
				m_targetWriteLock.unlock();
				m_closed = true;
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
}
