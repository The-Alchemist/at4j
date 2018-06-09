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
package org.at4j.zip.comp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.at4j.comp.CompressionLevel;
import org.at4j.comp.lzma.LzmaInputStream;
import org.at4j.comp.lzma.LzmaInputStreamSettings;
import org.at4j.comp.lzma.LzmaOutputStream;
import org.at4j.comp.lzma.LzmaOutputStreamSettings;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipFileParseException;
import org.at4j.zip.ZipGeneralPurposeBitFlags;
import org.entityfs.RandomAccess;

/**
 * This is the LZMA compression method. It is supported using the
 * {@link LzmaInputStream} implementation.
 * <p>
 * This class cannot be instantiated. Use the singleton instance
 * {@link #DEFAULT_INSTANCE} instead.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class LzmaCompressionMethod implements ZipEntryCompressionMethod, ZipEntryCompressionMethodFactory
{
	/**
	 * The unique code for this compression method. The code is used in the Zip
	 * entry's header to specify the compression method that is used for
	 * compressing the entry's file data.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(14);

	public static final String NAME = "LZMA";

	/**
	 * The PK-Zip version needed to extract entries compressed with this
	 * compression method. See
	 * {@link ZipEntryCompressionMethod#getVersionNeededToExtract()}.
	 */
	public static final UnsignedShort VERSION_NEEDED_TO_EXTRACT = UnsignedShort.valueOf(63);

	/**
	 * Singleton instance. This is initialized with the default settings.
	 */
	public static final LzmaCompressionMethod DEFAULT_INSTANCE = new LzmaCompressionMethod(new LzmaOutputStreamSettings().setWriteUncompressedDataSize(false));

	private static final byte[] LZMA_HEADER = new byte[] { (byte) 4, (byte) 57, (byte) 5, (byte) 0 };

	private final LzmaOutputStreamSettings m_settings;

	/**
	 * Create a new LZMA compression method object with settings for creating
	 * compression output streams.
	 * @param settings Settings for the compressing output streams created by
	 * this object. This has no effect on the decompressing input streams
	 * created by this object. This settings object should have its {@code
	 * writeUncompressedDataSize} property set to {@code false} and its {@code
	 * writeStreamProperties} property set to {@code true}.
	 * @throws IllegalArgumentException If the {@code writeUncompressedDataSize}
	 * property for the settings object is {@code true} or if the {@code
	 * writeStreamProperties} property is {@code false}.
	 */
	public LzmaCompressionMethod(LzmaOutputStreamSettings settings) throws IllegalArgumentException
	{
		if (settings.isWriteUncompressedDataSize())
		{
			throw new IllegalArgumentException("The writeUncompressedDataSize property must be false for the settings object");
		}
		else if (!settings.isWriteStreamProperties())
		{
			throw new IllegalArgumentException("The writeStreamProperties property must be true for the settings object");
		}

		m_settings = settings;
	}

	public UnsignedShort getCode()
	{
		return CODE;
	}

	public String getName()
	{
		return NAME;
	}

	public UnsignedShort getVersionNeededToExtract()
	{
		return VERSION_NEEDED_TO_EXTRACT;
	}

	public InputStream createInputStream(InputStream is, long compressedSize, long uncompressedSize) throws IOException
	{
		// Read the header
		// 2 bytes - LZMA SDK
		// 2 bytes - Size of properties (must be 5)
		// 5 bytes - LZMA decoder properties
		byte[] header = new byte[2 + 2];

		// Read past the first two + two bytes (LZMA SDK version and size of
		// properties)
		int noRead = is.read(header);
		if (noRead != 4)
		{
			throw new ZipFileParseException("Wanted to read 4 bytes. Got " + noRead);
		}

		int propertiesSize = UnsignedShort.fromBigEndianByteArray(header, 2).intValue();
		if (propertiesSize != 5)
		{
			throw new ZipFileParseException("Invalid size of LZMA property data: " + propertiesSize + " bytes. It must be 5 bytes");
		}

		byte[] properties = new byte[5];
		noRead = is.read(properties);
		if (noRead != 5)
		{
			throw new ZipFileParseException("Wanted to read 5 bytes. Got " + noRead);
		}

		LzmaInputStreamSettings settings = new LzmaInputStreamSettings();
		settings.setProperties(properties);
		settings.setReadUncompressedSize(false);
		settings.setUncompressedSize(uncompressedSize);

		return new LzmaInputStream(is, settings);
	}

	public OutputStream createOutputStream(OutputStream os) throws IOException, UnsupportedCompressionMethodException
	{
		// Write header (2 + 2 bytes)
		os.write(LZMA_HEADER);
		return new LzmaOutputStream(os, m_settings);
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException Always.
	 */
	public RandomAccess createRandomAccess(RandomAccess ra, long compressedSize, long uncompressedSize) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Random access is not supported for the LZMA compression method");
	}

	/**
	 * This method always returns {@code false}
	 * @return {@code false}, always.
	 */
	public boolean isRandomAccessSupported()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "LZMA";
	}

	public LzmaCompressionMethod create(ZipGeneralPurposeBitFlags gbBitFlags)
	{
		return this;
	}

	public ZipEntryCompressionMethod createWithCompressionLevel(CompressionLevel level)
	{
		return new LzmaCompressionMethod(new LzmaOutputStreamSettings().setCompressionLevel(level));
	}
}
