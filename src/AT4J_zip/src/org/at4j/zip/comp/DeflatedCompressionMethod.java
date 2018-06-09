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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.at4j.comp.CompressionLevel;
import org.at4j.support.lang.UnsignedShort;
import org.entityfs.RandomAccess;

/**
 * This is the "Deflated" compression method.
 * <p>
 * Use the {@link #getCompressionLevel()} to get the compression level for a
 * deflated Zip file entry.
 * <p>
 * <b>Important note when reading deflated Zip file entries:</b> Never try to
 * read more data than what is available in the stream. This <i>sometimes</i>
 * causes Java's {@link java.util.zip.InflaterOutputStream} to throw an
 * {@link java.io.EOFException} with the message "Unexpected end of ZLIB input
 * stream". See <a
 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4040920">this bug
 * report</a>.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class DeflatedCompressionMethod implements ZipEntryCompressionMethod
{
	/**
	 * The unique code for this compression method. The code is used in the Zip
	 * entry's header to specify the compression method that is used for
	 * compressing the entry's file data.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(8);

	public static final String NAME = "Deflated";

	/**
	 * The PK-Zip version needed to extract entries compressed with this
	 * compression method. See
	 * {@link ZipEntryCompressionMethod#getVersionNeededToExtract()}.
	 */
	public static final UnsignedShort VERSION_NEEDED_TO_EXTRACT = UnsignedShort.valueOf(20);

	/**
	 * The "normal" compression level.
	 */
	public static final int NORMAL_COMPRESSION_LEVEL = 6;

	/**
	 * The "fast" compression level.
	 */
	public static final int FAST_COMPRESSION_LEVEL = 3;

	/**
	 * The fastest compression level.
	 */
	public static final int FASTEST_COMPRESSION_LEVEL = 1;

	/**
	 * The maximum compression level.
	 */
	public static final int MAXIMUM_COMPRESSION_LEVEL = 9;

	/**
	 * A singleton instance that uses the fastest compression level.
	 */
	public static final DeflatedCompressionMethod FASTEST_COMPRESSION = new DeflatedCompressionMethod(DeflatedCompressionMethod.FASTEST_COMPRESSION_LEVEL);

	/**
	 * A singleton instance that uses the "fast" compression level.
	 */
	public static final DeflatedCompressionMethod FAST_COMPRESSION = new DeflatedCompressionMethod(DeflatedCompressionMethod.FAST_COMPRESSION_LEVEL);

	/**
	 * A singleton instance that uses the "normal" compression level.
	 */
	public static final DeflatedCompressionMethod NORMAL_COMPRESSION = new DeflatedCompressionMethod(DeflatedCompressionMethod.NORMAL_COMPRESSION_LEVEL);

	/**
	 * A singleton instance that uses the maximum compression level.
	 */
	public static final DeflatedCompressionMethod MAXIMUM_COMPRESSION = new DeflatedCompressionMethod(DeflatedCompressionMethod.MAXIMUM_COMPRESSION_LEVEL);

	private final int m_compressionLevel;

	/**
	 * Create a deflated compression method using the default compression level
	 * (6).
	 */
	public DeflatedCompressionMethod()
	{
		this(NORMAL_COMPRESSION_LEVEL);
	}

	/**
	 * Create a deflated compression method using the specified compression
	 * level.
	 * @param compressionLevel The compression level, between {@code 1}
	 * (fastest) and {@code 9} (best compression}.
	 * @throws IllegalArgumentException If the compression level is not in the
	 * range 1 to 9 (inclusive).
	 */
	public DeflatedCompressionMethod(int compressionLevel) throws IllegalArgumentException
	{
		if (compressionLevel < 1 || compressionLevel > 9)
		{
			throw new IllegalArgumentException("Invalid compression level " + compressionLevel + ". It must be between 1 and 9 (inclusive)");
		}
		m_compressionLevel = compressionLevel;
	}

	/**
	 * Create a deflated compression method using the specified compression
	 * level.
	 * @param level The compression level.
	 * @since 1.0.2
	 */
	public DeflatedCompressionMethod(CompressionLevel level)
	{
		switch (level)
		{
			case BEST:
				m_compressionLevel = MAXIMUM_COMPRESSION_LEVEL;
				break;
			case DEFAULT:
				m_compressionLevel = NORMAL_COMPRESSION_LEVEL;
				break;
			case FASTEST:
				m_compressionLevel = FASTEST_COMPRESSION_LEVEL;
				break;
			default:
				throw new RuntimeException("Unknown compression level " + level + ". This is a bug.");
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

	/**
	 * Get the compression level between 1 (fastest) and 9 (best) used by this
	 * compression method.
	 * @return The compression level.
	 */
	public int getCompressionLevel()
	{
		return m_compressionLevel;
	}

	private static class ExtraDummyByteAtTheEndStream extends FilterInputStream
	{
		private boolean m_haveReturnedDummyByte = false;
		
		private ExtraDummyByteAtTheEndStream(InputStream proxied)
		{
			super(proxied);
		}
		
		@Override
		public int read() throws IOException
		{
			int res = super.read();
			if (res < 0)
			{
				if (m_haveReturnedDummyByte)
				{
					return res;
				}
				else
				{
					m_haveReturnedDummyByte = true;
					return 0;
				}
			}
			else
			{
				return res;
			}
		}
		
		@Override
		public int read(byte[] barr) throws IOException
		{
			return read(barr, 0, barr.length);
		}
		
		@Override
		public int read(byte[] barr, int offset, int length) throws IOException
		{
			int res = super.read(barr, offset, length);
			if (res < 0)
			{
				if (m_haveReturnedDummyByte)
				{
					return res;
				}
				else
				{
					m_haveReturnedDummyByte = true;
					barr[0] = 0;
					return 1;
				}
			}
			else
			{
				return res;
			}
		}
	}
	
	public InputStream createInputStream(InputStream is, long compressedSize, long uncompressedSize)
	{
		// The deflated stream in a Zip file does not contain a header.
		Inflater inf = new Inflater(true);
		// Wrap the stream in a stream that will produce an extra 0 at the end
		// of the data. This is needed by the Inflater. See Inflater docs.
		return new InflaterInputStream(new ExtraDummyByteAtTheEndStream(is), inf);
	}

	public OutputStream createOutputStream(OutputStream os) throws IOException, UnsupportedCompressionMethodException
	{
		// The deflated stream in a Zip file does not contain a header.
		Deflater def = new Deflater(m_compressionLevel, true);
		return new DeflaterOutputStream(os, def);
	}

	/**
	 * This method always throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException Always.
	 */
	public RandomAccess createRandomAccess(RandomAccess ra, long compressedSize, long uncompressedSize) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Random access is not supported for the Deflate compression method");
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
		return "Deflated compression (" + m_compressionLevel + ")";
	}

	public ZipEntryCompressionMethod createWithCompressionLevel(CompressionLevel level)
	{
		return new DeflatedCompressionMethod(level);
	}
}
