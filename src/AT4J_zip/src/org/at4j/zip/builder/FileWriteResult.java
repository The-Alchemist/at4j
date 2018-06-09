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

import org.at4j.zip.comp.ZipEntryCompressionMethod;

/**
 * This is the result from writing a file to a Zip archive.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class FileWriteResult
{
	private final ZipEntryCompressionMethod m_compressionMethod;
	private final long m_crc32Checksum;
	private final long m_uncompressedSize;
	private final long m_compressedSize;

	/**
	 * Create a result object after writing a file to the Zip archive.
	 * @param cm The compression method that was used.
	 * @param crc32Checksum The CRC32 checksum of the file data before it was
	 * compressed.
	 * @param uncompressedSize The size of the file before it was compressed.
	 * @param compressedSize The size of the file in the Zip archive, after it
	 * was compressed.
	 * @throws IllegalArgumentException If either of the size values are
	 * negative.
	 */
	public FileWriteResult(ZipEntryCompressionMethod cm, long crc32Checksum, long uncompressedSize, long compressedSize) throws IllegalArgumentException
	{
		// Null check
		cm.getClass();

		if (uncompressedSize < 0)
		{
			throw new IllegalArgumentException("Invalid uncompressed size " + uncompressedSize + ". It must be >= 0");
		}
		if (compressedSize < 0)
		{
			throw new IllegalArgumentException("Invalid compressed size " + compressedSize + ". It must be >= 0");
		}

		m_compressionMethod = cm;
		m_crc32Checksum = crc32Checksum;
		m_uncompressedSize = uncompressedSize;
		m_compressedSize = compressedSize;
	}

	/**
	 * Get the compression method that was used when adding the file to the Zip
	 * archive.
	 * @return The compression method used.
	 */
	public ZipEntryCompressionMethod getCompressionMethod()
	{
		return m_compressionMethod;
	}

	/**
	 * Get the CRC32 checksum of the file data before it was compressed.
	 * @return The CRC32 checksum of the file data before it was compressed.
	 */
	public long getCrc32Checksum()
	{
		return m_crc32Checksum;
	}

	/**
	 * Get the size of the file before it was compressed.
	 * @return The size of the file before it was compressed.
	 */
	public long getUncompressedSize()
	{
		return m_uncompressedSize;
	}

	/**
	 * Get the size of the file when it has been compressed.
	 * @return The size of the file when it has been compressed.
	 */
	public long getCompressedSize()
	{
		return m_compressedSize;
	}
}
