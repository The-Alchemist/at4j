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
import org.at4j.comp.bzip2.BZip2InputStream;
import org.at4j.comp.bzip2.BZip2OutputStream;
import org.at4j.comp.bzip2.BZip2OutputStreamSettings;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipGeneralPurposeBitFlags;
import org.entityfs.RandomAccess;

/**
 * This is the "bzip2" compression method.
 * <p>
 * This class cannot be instantiated. Use the singleton instance
 * {@link #INSTANCE} instead.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class BZip2CompressionMethod implements ZipEntryCompressionMethod, ZipEntryCompressionMethodFactory
{
	private static final int BLOCK_SIZE_NOT_CONFIGURED = -1;

	/**
	 * The unique code for this compression method. The code is used in the Zip
	 * entry's header to specify the compression method that is used for
	 * compressing the entry's file data.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(12);

	public static final String NAME = "bzip2";

	/**
	 * The PK-Zip version needed to extract entries compressed with this
	 * compression method. See
	 * {@link ZipEntryCompressionMethod#getVersionNeededToExtract()}.
	 */
	public static final UnsignedShort VERSION_NEEDED_TO_EXTRACT = UnsignedShort.valueOf(46);

	/**
	 * Singleton instance. This is used instead of instantiating this class.
	 */
	public static final BZip2CompressionMethod INSTANCE = new BZip2CompressionMethod();

	private final int m_blockSize;

	/**
	 * This creates a bzip2 compression method that will use the default block
	 * size (900k) for compression.
	 * <p>
	 * Instead of calling this constructor, consider using the singleton
	 * instance {@link #INSTANCE} instead.
	 */
	public BZip2CompressionMethod()
	{
		m_blockSize = BLOCK_SIZE_NOT_CONFIGURED;
	}

	/**
	 * Create a bzip2 compression method that will use the supplied block size.
	 * @param blockSize The block size in hundreds of kilobytes. This number
	 * must be between 1 and 9 (inclusive).
	 * @throws IllegalArgumentException If the block size is not in the range
	 * {@code 1 <= blockSize <= 9}.
	 */
	public BZip2CompressionMethod(int blockSize) throws IllegalArgumentException
	{
		if ((blockSize < 1) || (blockSize > 9))
		{
			throw new IllegalArgumentException("Invalid block size. It must be a number between 1 and 9 (inclusive)");
		}
		m_blockSize = blockSize;
	}

	/**
	 * Create a bzip2 compression method that will have the supplied compression
	 * level.
	 * @param level The compression level.
	 * @since 1.0.2
	 */
	public BZip2CompressionMethod(CompressionLevel level)
	{
		switch (level)
		{
			case BEST:
				m_blockSize = 9;
				break;
			case DEFAULT:
				m_blockSize = BLOCK_SIZE_NOT_CONFIGURED;
				break;
			case FASTEST:
				m_blockSize = 1;
				break;
			default:
				throw new RuntimeException("Unknown compression level " + level + ". This is a bug");
		}
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
		return new BZip2InputStream(is);
	}

	public OutputStream createOutputStream(OutputStream os) throws IOException, UnsupportedCompressionMethodException
	{
		BZip2OutputStreamSettings settings = new BZip2OutputStreamSettings();
		if (m_blockSize != BLOCK_SIZE_NOT_CONFIGURED)
		{
			settings.setBlockSize(m_blockSize);
		}
		return new BZip2OutputStream(os, settings);
	}

	public BZip2CompressionMethod create(ZipGeneralPurposeBitFlags gbBitFlags)
	{
		return this;
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException Always.
	 */
	public RandomAccess createRandomAccess(RandomAccess ra, long compressedSize, long uncompressedSize) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Random access is not supported for the bzip2 compression method");
	}

	/**
	 * This method always returns {@code false}
	 * @return {@code false}, always.
	 */
	public boolean isRandomAccessSupported()
	{
		return false;
	}

	public ZipEntryCompressionMethod createWithCompressionLevel(CompressionLevel level)
	{
		return new BZip2CompressionMethod(level);
	}
	
	@Override
	public String toString()
	{
		return "BZip2";
	}
}
