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

import java.util.concurrent.ThreadFactory;

import org.at4j.comp.CompressionLevel;

/**
 * This configuration object contains settings for the {@link LzmaOutputStream}.
 * When new:ed, this object contains the default settings. Use setter methods to
 * modify its properties.
 * <p>
 * The properties are documented by their setter methods. For more in-depth
 * documentation, refer to the <a href="http://www.7-zip.org/sdk.html">LZMA
 * SDK</a> documentation.
 * @author Karl Gustafsson
 * @since 1.0
 * @see LzmaOutputStream
 */
public class LzmaOutputStreamSettings
{
	/**
	 * The default dictionary size exponent. See
	 * {@link #setDictionarySizeExponent(int)}.
	 */
	public static final int DEFAULT_DICTIONARY_SIZE_EXPONENT = 23;

	/**
	 * The default number of literal context bits. See
	 * {@link #setNumberOfLiteralContextBits(int)}.
	 */
	public static final int DEFAULT_NUMBER_OF_LITERAL_CONTEXT_BITS = 3;

	/**
	 * The default number of literal pos bits. See
	 * {@link #setNumberOfLiteralPosBits(int)}.
	 */
	public static final int DEFAULT_NUMBER_OF_LITERAL_POS_BITS = 0;

	/**
	 * The default number of pos bits. See {@link #setNumberOfPosBits(int)}.
	 */
	public static final int DEFAULT_NUMBER_OF_POS_BITS = 2;

	/**
	 * The default match finder algorithm. See
	 * {@link #setMatchFinderAlgorithm(MatchFinderAlgorithm)}.
	 */
	public static final MatchFinderAlgorithm DEFAULT_MATCH_FINDER_ALGORITHM = MatchFinderAlgorithm.BINARY_TREE_4;

	/**
	 * The default maximum number of entries in the data queue used to feed data
	 * to the encoder. See {@link #setMaxDataQueueSize(int)}.
	 */
	public static final int DEFAULT_MAX_DATA_QUEUE_SIZE = 0;

	/**
	 * By default the stream properties are written at the head of the stream.
	 * See {@link #setWriteStreamProperties(boolean)}.
	 */
	public static final boolean DEFAULT_WRITE_STREAM_PROPERTIES = true;

	/**
	 * By default the size of uncompressed data is written after the stream
	 * properties at the head of the stream. See
	 * {@link #setWriteUncompressedDataSize(boolean)}.
	 */
	public static final boolean DEFAULT_WRITE_UNCOMPRESSED_DATA_SIZE = true;

	static final int BUFFER_SIZE_NOT_SET = -1;

	private int m_dictionarySizeExponent = DEFAULT_DICTIONARY_SIZE_EXPONENT;
	private int m_numberOfLiteralContextBits = DEFAULT_NUMBER_OF_LITERAL_CONTEXT_BITS;
	private int m_numberOfLiteralPosBits = DEFAULT_NUMBER_OF_LITERAL_POS_BITS;
	private int m_numberOfPosBits = DEFAULT_NUMBER_OF_POS_BITS;
	private MatchFinderAlgorithm m_matchFinderAlgorithm = DEFAULT_MATCH_FINDER_ALGORITHM;
	private int m_maxDataQueueSize = DEFAULT_MAX_DATA_QUEUE_SIZE;
	private boolean m_writeStreamProperties = DEFAULT_WRITE_STREAM_PROPERTIES;
	private boolean m_writeUncompressedDataSize = DEFAULT_WRITE_UNCOMPRESSED_DATA_SIZE;
	private int m_bufferSize = BUFFER_SIZE_NOT_SET;
	private ThreadFactory m_threadFactory;

	/**
	 * Set the dictionary size exponent. The dictionary used will have the size
	 * {@code 2^size} bytes. The maximum exponent is 30. The default is {@code
	 * 2^23} = 8.4 MB.
	 * @param size The dictionary size exponent.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the supplied size is negative or
	 * larger than 30.
	 */
	public LzmaOutputStreamSettings setDictionarySizeExponent(int size) throws IllegalArgumentException
	{
		if (size < 0 || size > 30)
		{
			throw new IllegalArgumentException("Illegal dictionary size " + size + ". It must be between 0 and 30 (inclusive).");
		}
		m_dictionarySizeExponent = size;
		return this;
	}

	/**
	 * Get the dictionary size exponent.
	 * @return The dictionary size exponent.
	 * @see #setDictionarySizeExponent(int)
	 */
	public int getDictionarySizeExponent()
	{
		return m_dictionarySizeExponent;
	}

	/**
	 * Set the number of literal context bits. See the LZMA SDK documentation
	 * for an explanation of this property.
	 * <p>
	 * This value must be between 0 and 8 (inclusive). The default value is 3.
	 * According to the LZMA documentation, 4 may give a better compress ratio
	 * for big files.
	 * @param no The number of literal context bits.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the number is negative or greater
	 * than 8.
	 */
	public LzmaOutputStreamSettings setNumberOfLiteralContextBits(int no) throws IllegalArgumentException
	{
		if (no < 0 || no > 8)
		{
			throw new IllegalArgumentException("Illegal number of context bits " + no + ". The value must be between 0 and 8 (inclusive).");
		}
		m_numberOfLiteralContextBits = no;
		return this;
	}

	/**
	 * Get the number of literal context bits.
	 * @return The number of literal context bits.
	 * @see #setNumberOfLiteralContextBits(int)
	 */
	public int getNumberOfLiteralContextBits()
	{
		return m_numberOfLiteralContextBits;
	}

	/**
	 * Set the number of literal pos bits. This is used for periodical data when
	 * the period is equal to {@code 2^no} bytes. For example, for 32-bit (4
	 * bytes) periodical data, {@code no} can be set to 2.
	 * <p>
	 * If this property is modified from its default value of 0, it often makes
	 * sense to set the number of literal context bits to 0. See
	 * {@link #setNumberOfLiteralContextBits(int)}.
	 * <p>
	 * This property must be in the range between 0 and 4 (inclusive).
	 * @param no The number of literal pos bits.
	 * @return {@code this}.
	 * @throws IllegalArgumentException If the number is negative or greater
	 * than 4.
	 */
	public LzmaOutputStreamSettings setNumberOfLiteralPosBits(int no) throws IllegalArgumentException
	{
		if (no < 0 || no > 4)
		{
			throw new IllegalArgumentException("Illegal number of literal pos bits " + no + " The value must be between 0 and 4 (inclusive).");
		}
		m_numberOfLiteralPosBits = no;
		return this;
	}

	/**
	 * Get the number of literal pos bits.
	 * @return The number of literal pos bits.
	 * @see #setNumberOfLiteralPosBits(int)
	 */
	public int getNumberOfLiteralPosBits()
	{
		return m_numberOfLiteralPosBits;
	}

	/**
	 * Set the number of pos bits. This is intended for periodical data when the
	 * period is equal to {@code 2^no}.
	 * <p>
	 * This value must be between 0 and 4 (inclusive). The default value is 2.
	 * @param no The number of pos bits.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the number is negative or greater
	 * than 4.
	 */
	public LzmaOutputStreamSettings setNumberOfPosBits(int no) throws IllegalArgumentException
	{
		if (no < 0 || no > 4)
		{
			throw new IllegalArgumentException("Illegal number of pos bits " + no + ". The value must be between 0 and 4 (inclusive).");
		}
		m_numberOfPosBits = no;
		return this;
	}

	/**
	 * Get the number of pos bits.
	 * @return The number of pos bits.
	 * @see #setNumberOfPosBits(int)
	 */
	public int getNumberOfPosBits()
	{
		return m_numberOfPosBits;
	}

	/**
	 * Set the match finder algorithm for the encoder.
	 * @param mfa The match finder algorithm.
	 * @return {@code this}
	 * @throws NullPointerException If the match finder algorithm is {@code
	 * null}.
	 */
	public LzmaOutputStreamSettings setMatchFinderAlgorithm(MatchFinderAlgorithm mfa) throws NullPointerException
	{
		// Null check
		mfa.getClass();
		m_matchFinderAlgorithm = mfa;
		return this;
	}

	/**
	 * Get the match finder algorithm for the encoder.
	 * @return The match finder algorithm for the encoder.
	 * @see #setMatchFinderAlgorithm(MatchFinderAlgorithm)
	 */
	public MatchFinderAlgorithm getMatchFinderAlgorithm()
	{
		return m_matchFinderAlgorithm;
	}

	/**
	 * Set the maximum queue depth for the data queue used to feed data to the
	 * encoder. By default the maximum queue depth is unlimited (represented by
	 * a value of 0). By setting this to some value, the total memory
	 * requirement for the compression operation is capped. The drawback is that
	 * the writing thread may be blocked waiting for the permission to put data
	 * on the queue if the queue is full.
	 * <p>
	 * The queue depth is measured in the number of data records in the queue.
	 * Every time the writing thread writes to the LZMA output stream, a data
	 * entry is put on the queue. This may be a single byte or a large byte
	 * array.
	 * @param max The maximum queue depth measured in the number of data
	 * entries. 0 means that the queue has unlimited size.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the maximum queue depth is negative.
	 */
	public LzmaOutputStreamSettings setMaxDataQueueSize(int max) throws IllegalArgumentException
	{
		if (max < 0)
		{
			throw new IllegalArgumentException("Invalid max size " + max);
		}
		m_maxDataQueueSize = max;
		return this;
	}

	/**
	 * Get the maximum size for the data queue used to feed data to the encoder.
	 * @return The maximum size for the data queue used to feed data to the
	 * encoder.
	 * @see #setMaxDataQueueSize(int)
	 */
	public int getMaxDataQueueSize()
	{
		return m_maxDataQueueSize;
	}

	/**
	 * Should the encoding properties be written at the head of the stream? The
	 * properties are five bytes that document the settings of the encoder when
	 * encoding the stream. They are used by the decoder to be able to decode
	 * the stream.
	 * <p>
	 * If the properties are set somewhere else, this may be set to {@code
	 * false}.
	 * <p>
	 * The default behavior is to write the encoder properties.
	 * @param b Should the encoding properties be written at the head of the
	 * stream?
	 * @return {@code this}
	 * @see #setWriteUncompressedDataSize(boolean)
	 */
	public LzmaOutputStreamSettings setWriteStreamProperties(boolean b)
	{
		m_writeStreamProperties = b;
		return this;
	}

	/**
	 * Should the encoder properties be written at the head of the stream?
	 * @return {@code true} if the encoder properties should be written at the
	 * head of the stream.
	 * @see #setWriteStreamProperties(boolean)
	 */
	public boolean isWriteStreamProperties()
	{
		return m_writeStreamProperties;
	}

	/**
	 * Should the uncompressed data size be written at the head of the stream,
	 * after the encoder properties (or first, if they are not present)? The
	 * uncompressed data size is a eight byte signed little endian value.
	 * <p>
	 * The default behavior is to write the uncompressed data size, or to write
	 * {@code -1} if that is unknown.
	 * @param b Should the uncompressed data size be written at the head of the
	 * stream?
	 * @return {@code this}
	 */
	public LzmaOutputStreamSettings setWriteUncompressedDataSize(boolean b)
	{
		m_writeUncompressedDataSize = b;
		return this;
	}

	/**
	 * Should the uncompressed data size (even if unknown) be written after the
	 * encoder properties at the head of the stream?
	 * @return {@code true} if the uncompressed data size should be written at
	 * the head of the stream.
	 * @see #setWriteUncompressedDataSize(boolean)
	 */
	public boolean isWriteUncompressedDataSize()
	{
		return m_writeUncompressedDataSize;
	}

	/**
	 * Set the buffer size to use for the buffered output stream that the LZMA
	 * stream is opened on.
	 * @param sz The buffer size.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the buffer size is {@code < 1}.
	 */
	public LzmaOutputStreamSettings setBufferSize(int sz) throws IllegalArgumentException
	{
		if (sz < 1)
		{
			throw new IllegalArgumentException("Invalid buffer size " + sz + ". It must be > 0");
		}
		m_bufferSize = sz;
		return this;
	}

	/**
	 * Get the buffer size to use for the buffered output stream that the LZMA
	 * stream is opened on.
	 * @return The buffer size.
	 * @see #setBufferSize(int)
	 */
	public int getBufferSize()
	{
		return m_bufferSize;
	}

	/**
	 * Set a compression level. Setting a compression level affects some of the
	 * properties of this object.
	 * <p>
	 * The following values are set for the different compression levels:
	 * <table>
	 * <th>
	 * <td>Property</td>
	 * <td>BEST</td>
	 * <td>DEFAULT</td>
	 * <td>FASTEST</td></th>
	 * <tr>
	 * <td>Dictionary size exponent</td>
	 * <td>30 bytes</td>
	 * <td>23 bytes</td>
	 * <td>20 bytes</td>
	 * </tr>
	 * <tr>
	 * <td>Number of literal context bits</td>
	 * <td>4</td>
	 * <td>3</td>
	 * <td>3</td>
	 * </tr>
	 * </table>
	 * @param level The compression level.
	 * @return {@code this}
	 * @since 1.0.2
	 */
	public LzmaOutputStreamSettings setCompressionLevel(CompressionLevel level)
	{
		switch (level)
		{
			case BEST:
				m_dictionarySizeExponent = 30;
				m_numberOfLiteralContextBits = 4;
				break;
			case DEFAULT:
				m_dictionarySizeExponent = DEFAULT_DICTIONARY_SIZE_EXPONENT;
				m_numberOfLiteralContextBits = 3;
				break;
			case FASTEST:
				m_dictionarySizeExponent = 20;
				m_numberOfLiteralContextBits = 3;
				break;
			default:
				throw new RuntimeException("Unknown compression level. This is a bug.");
		}
		return this;
	}

	/**
	 * Set the thread factory to use for creating the thread that will run the
	 * LZMA compression.
	 * <p>
	 * If this is not set, the {@link LzmaOutputStream} will just create a
	 * thread with {@code new Thread}.
	 * <p>
	 * By default, this property is {@code null}.
	 * @param tf The thread factory, or {@code null} if the
	 * {@link LzmaOutputStream} should use {@code new Thread} to create the
	 * compression thread.
	 * @return {@code this}
	 */
	public LzmaOutputStreamSettings setThreadFactory(ThreadFactory tf)
	{
		m_threadFactory = tf;
		return this;
	}

	/**
	 * Get the thread factory that will be used for creating the LZMA
	 * compression thread.
	 * @return The thread factory, or {@code null} if not set.
	 */
	public ThreadFactory getThreadFactory()
	{
		return m_threadFactory;
	}

	/**
	 * This method can be used to get the output stream properties in the format
	 * that they are encoded in the stream.
	 * <p>
	 * The properties can be used with a {@link LzmaInputStreamSettings} object.
	 * @return The encoded properties.
	 */
	public byte[] getProperties()
	{
		byte[] res = new byte[5];
		res[0] = (byte) ((m_numberOfPosBits * 5 + m_numberOfLiteralPosBits) * 9 + m_numberOfLiteralContextBits);
		int dictionarySize = 1 << m_dictionarySizeExponent;
		for (int i = 0; i < 4; i++)
		{
			res[1 + i] = (byte) (dictionarySize >> (8 * i));
		}
		return res;
	}
}
