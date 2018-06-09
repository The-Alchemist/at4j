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
package org.at4j.comp.lzma;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import org.at4j.support.lang.At4JException;

import SevenZip.Compression.LZMA.Encoder;

/**
 * This class provides an {@link java.io.OutputStream} for encoding data using
 * the Lempel-Ziv-Markov chain algorithm. It uses the LZMA encoder from <a
 * href="http://www.7-zip.org/sdk.html">the LZMA SDK</a>.
 * <p>
 * The data written by this encoder is has the following structure:
 * <ol>
 * <li>Settings used when encoding (5 bytes). <i>Optional. Enabled by
 * default.</i></li>
 * <li>Total size of the data in the stream when uncompressed (8 bytes, little
 * endian). If the uncompressed size is not known, this is set to -1 and the
 * data part will be followed by an end of stream marker. <i>Optional. Enabled
 * by default.</i></li>
 * <li>Data</li>
 * <li>End of stream marker (if the total data size was set to -1).</li>
 * </ol>
 * <p>
 * The API from the LZMA SDK is built around a standalone encoder. To adapt that
 * into the Java streams API, the encoder is launched in a separate execution
 * thread and it is fed data as it is written to the stream. When the stream is
 * closed, the encoder finishes and the encoder thread is shut down.
 * <p>
 * Errors are propagated up from the encoder to the calling thread. If an error
 * occurs in the encoder, it will be reported to the calling thread the next
 * time that it tries to write to or close the stream.
 * <p>
 * If the application that uses this stream writes small data chunks to it, it
 * is probably a good idea to wrap it in a {@link java.io.BufferedOutputStream}
 * to ensure that the LZMA encoder gets bigger chunks of data to work with.
 * <p>
 * Compressing a file requires a lot of memory since the compression dictionary
 * has to be held in memory. The default size of the dictionary is
 * 2^23&nbsp;=&nbsp;8.4&nbsp;MB. By default, the buffer used to feed data to the
 * encoder has an unlimited size, which may cause the entire written data to be
 * stored in memory if the writing thread is fast and the encoder is slow. A
 * maximum size for the buffer can be set by using a custom data queue depth
 * when creating the compressing stream. See
 * {@link LzmaOutputStreamSettings#setMaxDataQueueSize(int)}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see LzmaOutputStreamSettings
 * @see LzmaInputStream
 */
public final class LzmaOutputStream extends OutputStream
{
	private final Thread m_writerThread;
	private final LzmaErrorState m_errorState;
	private final LinkedBlockingQueue<byte[]> m_dataQueue;
	private final OutputStream m_wrapped;

	private boolean m_closed;

	/**
	 * Create a new LZMA compressing output stream using default compression
	 * settings. The default settings should be good for most compression cases.
	 * <p>
	 * This method will set the uncompressed data size to {@code -1} in the
	 * compressed stream and will write an end of stream marker after all data.
	 * @param out The output stream to write compressed data to.
	 * @see #LzmaOutputStream(OutputStream, LzmaOutputStreamSettings)
	 */
	public LzmaOutputStream(OutputStream out)
	{
		this(out, new LzmaOutputStreamSettings(), -1L);
	}

	/**
	 * Create a new LZMA compressing output stream using custom compression
	 * settings.
	 * <p>
	 * This method will set the uncompressed data size to -1 in the compressed
	 * stream and will write an end of stream marker after all data.
	 * @param out The output stream to write compressed data to.
	 * @param settings Compression settings.
	 * @see #LzmaOutputStream(OutputStream)
	 * @see #LzmaOutputStream(OutputStream, LzmaOutputStreamSettings, long)
	 */
	public LzmaOutputStream(OutputStream out, LzmaOutputStreamSettings settings)
	{
		this(out, settings, -1L);
	}

	/**
	 * Create a new LZMA compressing output stream using custom compression
	 * settings that compresses data of a known size.
	 * @param out The output stream to write compressed data to.
	 * @param settings Compression settings.
	 * @param uncompressedDataSize The size of the uncompressed data. Set this
	 * to {@code -1} (or rather, use any of the other constructors) if the data
	 * size is unknown.
	 * @throws IllegalArgumentException If the data size is zero or less than
	 * -1.
	 */
	public LzmaOutputStream(OutputStream out, LzmaOutputStreamSettings settings, long uncompressedDataSize) throws IllegalArgumentException
	{
		if (uncompressedDataSize == 0 || uncompressedDataSize < -1)
		{
			throw new IllegalArgumentException("Illegal data size: " + uncompressedDataSize);
		}

		// Create the encoder here rather than in the LzmaWriterRunnable to be
		// able to detect errors before launching the separate thread.
		Encoder enc = new Encoder();
		enc.SetDictionarySize(1 << settings.getDictionarySizeExponent());
		enc.SetLcLpPb(settings.getNumberOfLiteralContextBits(), settings.getNumberOfLiteralPosBits(), settings.getNumberOfPosBits());
		enc.SetMatchFinder(settings.getMatchFinderAlgorithm().getId());
		enc.SetEndMarkerMode(uncompressedDataSize == -1);

		m_dataQueue = settings.getMaxDataQueueSize() != 0 ? new LinkedBlockingQueue<byte[]>(settings.getMaxDataQueueSize()) : new LinkedBlockingQueue<byte[]>();
		m_errorState = new LzmaErrorState();
		m_wrapped = out;
		Runnable encoderRunnable = new LzmaWriterRunnable(new LzmaEncoderInputStream(m_dataQueue), enc, settings, uncompressedDataSize, m_errorState, out);
		m_writerThread = settings.getThreadFactory() != null ? settings.getThreadFactory().newThread(encoderRunnable) : new Thread(encoderRunnable);
		m_writerThread.start();
	}

	private void assertNotClosed() throws IOException
	{
		if (m_closed)
		{
			throw new IOException("This stream is closed");
		}
	}

	private void put(byte[] barr)
	{
		try
		{
			m_dataQueue.put(barr);
		}
		catch (InterruptedException e)
		{
			// Should not happen. The queue has an unlimited size
			throw new At4JException(e);
		}
	}

	@Override
	public void write(int b) throws IOException
	{
		assertNotClosed();
		m_errorState.testAndClearErrors();
		put(new byte[] { (byte) (b & 0xFF) });
	}

	@Override
	public void write(byte[] barr) throws IOException
	{
		assertNotClosed();
		m_errorState.testAndClearErrors();

		// Make a defensive copy of the supplied array since its content is
		// mutable
		byte[] barr2 = new byte[barr.length];
		System.arraycopy(barr, 0, barr2, 0, barr.length);
		put(barr2);
	}

	@Override
	public void write(byte[] barr, int offset, int len) throws IOException
	{
		assertNotClosed();
		if (offset < 0)
		{
			throw new IndexOutOfBoundsException("Illegal offset " + offset);
		}
		else if (len < 0)
		{
			throw new IndexOutOfBoundsException("Illegal length " + len);
		}
		else if (offset + len > barr.length)
		{
			throw new IndexOutOfBoundsException("Offset + length (" + offset + " + " + len + ") is greater than the length of the supplied array (" + barr.length + ")");
		}
		m_errorState.testAndClearErrors();

		// Make a defensive copy of the supplied array since its content is
		// mutable
		byte[] barr2 = new byte[len];
		System.arraycopy(barr, offset, barr2, 0, len);
		put(barr2);
	}

	@Override
	public void close() throws IOException
	{
		if (!m_closed)
		{
			try
			{
				try
				{
					// Write an EOF marker to the data queue and wait for the encoder
					// to finish
					put(new byte[0]);

					try
					{
						m_writerThread.join();
					}
					catch (InterruptedException e)
					{
						throw new IOException(e.toString());
					}
				}
				finally
				{
					m_wrapped.close();
				}
			}
			finally
			{
				m_closed = true;
				super.close();
			}
			m_errorState.testAndClearErrors();
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}
}
