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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import org.entityfs.RandomAccess;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.ChecksumInputStream;
import org.entityfs.support.io.CountingInputStream;
import org.entityfs.support.io.CountingOutputStream;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.io.RandomAccessToOutputStreamAdapter;

/**
 * This is a {@link FileWriteStrategy} that is used by the {@link ZipBuilder}
 * when data read from a stream should be added as a file entity to a Zip
 * archive.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class StreamFileWriteStrategy implements FileWriteStrategy
{
	private final InputStream m_stream;
	private final ZipEntrySettings m_effectiveSettings;

	/**
	 * @param is This stream is not closed by this object.
	 */
	public StreamFileWriteStrategy(InputStream is, ZipEntrySettings effectiveSettings)
	{
		// Null checks
		is.getClass();
		effectiveSettings.getClass();

		m_stream = is;
		m_effectiveSettings = effectiveSettings;
	}

	public FileWriteResult writeFile(RandomAccess out) throws IOException
	{
		long compressedSize;
		CRC32 checksum = new CRC32();
		// Count the number of bytes read
		CountingInputStream cis = new CountingInputStream(m_stream);
		// Calculate the CRC32 checksum for the uncompressed file
		ChecksumInputStream<CRC32> chis = new ChecksumInputStream<CRC32>(cis, checksum);
		try
		{
			// Count the number of bytes written.
			// Instruct the adapter to not close the underlying RandomAccess
			// when closed
			CountingOutputStream cos = new CountingOutputStream(new RandomAccessToOutputStreamAdapter(out, false));
			OutputStream os = m_effectiveSettings.getCompressionMethod().createOutputStream(cos);
			try
			{
				StreamUtil.copyStreams(chis, os, 16384);
			}
			catch (WrappedIOException e)
			{
				// Un-wrap
				throw e.getWrapped();
			}
			finally
			{
				// This does not close the RandomAccess
				os.close();
			}
			compressedSize = cos.getNoOfBytesWritten();
		}
		finally
		{
			chis.close();
		}
		long uncompressedSize = cis.getNoOfBytesReadOrSkipped();
		// TODO: use a temporary file to uncompress the contents if it turns out
		// that the uncompressed version was smaller.
		return new FileWriteResult(m_effectiveSettings.getCompressionMethod(), checksum.getValue(), uncompressedSize, compressedSize);
	}
}
