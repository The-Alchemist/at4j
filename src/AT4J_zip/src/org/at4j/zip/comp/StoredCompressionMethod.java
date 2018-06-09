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
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.ZipGeneralPurposeBitFlags;
import org.entityfs.RandomAccess;

/**
 * "Stored" compression means that the file is stored as-is, i.e. not
 * compressed, in the Zip archive.
 * <p>
 * This class cannot be instantiated. Use the singleton instance
 * {@link #INSTANCE} instead.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class StoredCompressionMethod implements ZipEntryCompressionMethod, ZipEntryCompressionMethodFactory
{
	/**
	 * The unique code for this compression method. The code is used in the Zip
	 * entry's header to specify the compression method that is used for
	 * compressing the entry's file data.
	 */
	public static final UnsignedShort CODE = UnsignedShort.valueOf(0);

	public static final String NAME = "Stored";

	/**
	 * The PK-Zip version needed to extract entries compressed with this
	 * compression method. See
	 * {@link ZipEntryCompressionMethod#getVersionNeededToExtract()}.
	 */
	public static final UnsignedShort VERSION_NEEDED_TO_EXTRACT = UnsignedShort.valueOf(10);

	/**
	 * Singleton instance. This is used instead of instantiating this class.
	 */
	public static final StoredCompressionMethod INSTANCE = new StoredCompressionMethod();

	/** Hidden constructor. */
	private StoredCompressionMethod()
	{
		// Nothing
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

	public InputStream createInputStream(InputStream is, long compressedSize, long uncompressedSize)
	{
		// This was easy
		return is;
	}

	public OutputStream createOutputStream(OutputStream os) throws IOException, UnsupportedCompressionMethodException
	{
		// This was easy
		return os;
	}

	public RandomAccess createRandomAccess(RandomAccess ra, long compressedSize, long uncompressedSize) throws UnsupportedOperationException
	{
		// Easy
		return ra;
	}

	/**
	 * This method always returns {@code true}
	 * @return {@code true}, always.
	 */
	public boolean isRandomAccessSupported()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "Stored (no compression)";
	}

	public StoredCompressionMethod create(ZipGeneralPurposeBitFlags gbBitFlags)
	{
		return INSTANCE;
	}

	/**
	 * @return {@code this}
	 */
	public ZipEntryCompressionMethod createWithCompressionLevel(CompressionLevel level)
	{
		return this;
	}
}
