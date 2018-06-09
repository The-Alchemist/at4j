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

import org.at4j.zip.comp.StoredCompressionMethod;
import org.entityfs.RandomAccess;
import org.entityfs.ReadableFile;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.ChecksumInputStream;
import org.entityfs.support.io.CountingOutputStream;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.io.RandomAccessToOutputStreamAdapter;

/**
 * This strategy is for files that can be reread if necessary. First it writes
 * the file using the selected compression method. If the compressed file turns
 * out to be larger than the original file, it erases it an writes the file
 * uncompressed instead.
 * <p>
 * This object is used by the {@link ZipBuilder}.
 * @author Karl Gustafsson
 * @since 1.0
 */
final class RereadableFileWriteStrategy implements FileWriteStrategy
{
	private final ReadableFile m_file;
	private final ZipEntrySettings m_effectiveSettings;

	/**
	 * @param f Must be locked for reading by the calling thread during the
	 * lifetime of this object.
	 */
	public RereadableFileWriteStrategy(ReadableFile f, ZipEntrySettings effectiveSettings)
	{
		// Null checks
		f.getClass();
		effectiveSettings.getClass();

		m_file = f;
		m_effectiveSettings = effectiveSettings;
	}

	public FileWriteResult writeFile(RandomAccess out) throws IOException
	{
		long startPos = out.getFilePointer();

		long compressedSize, uncompressedSize;
		CRC32 checksum = new CRC32();
		InputStream is = m_file.openForRead();
		// Calculate the CRC32 checksum for the uncompressed file
		ChecksumInputStream<CRC32> chis = new ChecksumInputStream<CRC32>(is, checksum);
		try
		{
			// Count the number of bytes written.
			// Instruct the adapter to not close the underlying RandomAccess
			// when closed
			CountingOutputStream cos = new CountingOutputStream(new RandomAccessToOutputStreamAdapter(out, false));
			OutputStream os = m_effectiveSettings.getCompressionMethod().createOutputStream(cos);
			try
			{
				uncompressedSize = StreamUtil.copyStreams(chis, os, 16384);
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

		// Analyze the result. Did the file size decrease when compressed?
		if ((m_effectiveSettings.getCompressionMethod() instanceof StoredCompressionMethod) || (compressedSize < uncompressedSize))
		{
			// Yes, it did. We're happy.
			return new FileWriteResult(m_effectiveSettings.getCompressionMethod(), checksum.getValue(), uncompressedSize, compressedSize);
		}
		else
		{
			// No, the file size did not decrease. Add the file uncompressed
			// instead.

			// Rewind to the starting point
			out.setLength(startPos);
			is = m_file.openForRead();
			try
			{
				// Don't close the underlying RandomAccess
				OutputStream os = new RandomAccessToOutputStreamAdapter(out, false);
				try
				{
					StreamUtil.copyStreams(is, os, 16384);
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
			}
			finally
			{
				is.close();
			}

			return new FileWriteResult(StoredCompressionMethod.INSTANCE, checksum.getValue(), uncompressedSize, uncompressedSize);
		}
	}
}
