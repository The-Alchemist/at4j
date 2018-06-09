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

import java.io.InputStream;
import java.io.OutputStream;

import org.at4j.comp.CompressionLevel;
import org.entityfs.RandomAccess;

/**
 * This class may be inherited by unsupported compression method.
 * @author Karl Gustafsson
 * @since 1.0.2
 */
public abstract class AbstractUnsupportedCompressionMethod implements ZipEntryCompressionMethod
{
	/**
	 * This method always throws an
	 * {@link UnsupportedCompressionMethodException}.
	 * @throws UnsupportedCompressionMethodException Always.
	 */
	public final InputStream createInputStream(InputStream is, long compressedSize, long uncompressedSize) throws UnsupportedCompressionMethodException
	{
		throw new UnsupportedCompressionMethodException(this);
	}

	/**
	 * This method always throws an
	 * {@link UnsupportedCompressionMethodException}.
	 * @throws UnsupportedCompressionMethodException Always.
	 */
	public final OutputStream createOutputStream(OutputStream os) throws UnsupportedCompressionMethodException
	{
		throw new UnsupportedCompressionMethodException(this);
	}

	/**
	 * This method always throws an
	 * {@link UnsupportedCompressionMethodException}.
	 * @throws UnsupportedCompressionMethodException Always.
	 */
	public final RandomAccess createRandomAccess(RandomAccess ra, long compressedSize, long uncompressedSize) throws UnsupportedCompressionMethodException
	{
		throw new UnsupportedCompressionMethodException(this);
	}

	/**
	 * This method always returns {@code false}
	 * @return {@code false}, always.
	 */
	public final boolean isRandomAccessSupported()
	{
		return false;
	}

	/**
	 * @return {@code this}
	 */
	public final ZipEntryCompressionMethod createWithCompressionLevel(CompressionLevel level)
	{
		return this;
	}
}
