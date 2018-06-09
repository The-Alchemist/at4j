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
import org.at4j.zip.ZipFileParseException;
import org.entityfs.RandomAccess;

/**
 * This interface defines a file compression method that may occur in a Zip
 * file. Zip compression method objects is used by the
 * {@link org.at4j.zip.ZipFile} object to handle decompression of file data, and
 * by the {@link org.at4j.zip.builder.ZipBuilder} to handle compression of file
 * data.
 * <p>
 * Each compression method is identified by a unique code. The
 * {@link ZipEntryCompressionMethodRegistry} can be used to look up a
 * compression method by its code.
 * <p>
 * Some implementations of this interface does not have any configurable
 * internal state and can thus be used as singleton objects. All implementations
 * are immutable.
 * <p>
 * Implementations are encouraged to implement a {@code toString} method that
 * returns a human-readable name of the compression method.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntryCompressionMethodRegistry
 * @see ZipEntryCompressionMethodFactory
 */
public interface ZipEntryCompressionMethod
{
	/**
	 * Get the unique code that is used to identify this compression method in a
	 * Zip entry's header.
	 * @return The compression method's unique code.
	 */
	UnsignedShort getCode();

	/**
	 * Get a human-readable name for the compression method.
	 * @return The compression method's name.
	 */
	String getName();

	/**
	 * Get the PK-Zip version needed to extract Zip entries compressed by this
	 * compression method.
	 * @return An {@link UnsignedShort} representing the version number. {@code
	 * 10} corresponds to version 1.0, {@code 62} to 6.2, etc.
	 */
	UnsignedShort getVersionNeededToExtract();

	/**
	 * Create an {@code InputStream} that clients can use to read uncompressed
	 * file data from.
	 * @param is An open {@code InputStream} on the compressed file data.
	 * @param compressedSize The total size of the compressed data.
	 * @param uncompressedSize The total size of the uncompressed data.
	 * @return An open {@code InputStream} containing uncompressed file data.
	 * @throws IOException On I/O errors.
	 * @throws UnsupportedCompressionMethodException If the compression method
	 * is not supported.
	 * @throws ZipFileParseException On parse errors.
	 */
	InputStream createInputStream(InputStream is, long compressedSize, long uncompressedSize) throws IOException, UnsupportedCompressionMethodException, ZipFileParseException;

	/**
	 * Does this compression method support read only random access on
	 * uncompressed file data?
	 * <p>
	 * Random access is only supported by the compression methods that don't
	 * have a position-based state for the compressed data (which currently all
	 * compressing compression methods have). In other words, this is only
	 * supported if the file is stored uncompressed in the archive using the
	 * {@link StoredCompressionMethod}.
	 * @return {@code true} if the compression method supports read only random
	 * access on file data.
	 */
	boolean isRandomAccessSupported();

	/**
	 * Open a read only random access object on the file entry's data.
	 * @param ra An open random access object on the file entry's data.
	 * @param compressedSize The total size of the compressed data.
	 * @param uncompressedSize The total size of the uncompressed data.
	 * @return On open {@code RandomAccess} that can be used to read
	 * uncompressed data.
	 * @throws IOException On I/O errors.
	 * @throws UnsupportedCompressionMethodException If the compression method
	 * is not implemented.
	 * @throws UnsupportedOperationException If the compression method does not
	 * support random access to uncompressed file data.
	 * @throws ZipFileParseException On parse errors.
	 */
	RandomAccess createRandomAccess(RandomAccess ra, long compressedSize, long uncompressedSize) throws IOException, UnsupportedCompressionMethodException, UnsupportedOperationException, ZipFileParseException;

	/**
	 * Create an {@link OutputStream} that compresses the data written to it and
	 * writes it to the wrapped output stream. This is used when building Zip
	 * files.
	 * @param os The wrapped output stream.
	 * @return A compressing output stream.
	 * @throws IOException On I/O errors.
	 * @throws UnsupportedCompressionMethodException If the compression method
	 * is not supported.
	 */
	OutputStream createOutputStream(OutputStream os) throws IOException, UnsupportedCompressionMethodException;

	/**
	 * Create a new {@link ZipEntryCompressionMethod} object that will have the
	 * supplied compression level.
	 * <p>
	 * The reason for this method is that it makes it easier to set a custom
	 * compression level for a compression method, without having to create a
	 * new compression method object from scratch manually.
	 * @param level The compression level.
	 * @return A new compression method object. If the new object would have
	 * been created with the same settings as this object, this method may
	 * return {@code this} since {@link ZipEntryCompressionMethod} objects are
	 * immutable.
	 * @since 1.0.2
	 */
	ZipEntryCompressionMethod createWithCompressionLevel(CompressionLevel level);
}
