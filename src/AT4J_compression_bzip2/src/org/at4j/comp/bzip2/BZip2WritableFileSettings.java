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
package org.at4j.comp.bzip2;

import org.at4j.support.lang.At4JException;
import org.entityfs.support.log.LogAdapter;

/**
 * This object contains settings used when creating a {@link BZip2WritableFile}.
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BZip2WritableFileSettings implements Cloneable
{
	private int m_blockSize = BZip2OutputStreamSettings.DEFAULT_BLOCK_SIZE;
	private int m_bufferSize = BZip2WritableFile.BUFFER_SIZE_NOT_SET;
	private boolean m_useCommonsCompress;
	private LogAdapter m_logAdapter;
	private int m_numberOfEncoderThreads = 0;
	private BZip2EncoderExecutorService m_executorService;
	private int m_numberOfHuffmanTreeRefinementIterations = BZip2OutputStreamSettings.DEFAULT_NO_OF_HUFFMAN_TREE_REFINEMENT_ITERATIONS;

	/**
	 * Set the block size for the compression.
	 * @param blockSize The size of bzip2 dictionary in 100k units. This must be
	 * a value between {@code 1} and {@code 9} (inclusive). A higher value gives
	 * a higher compression, but it will also make the compression and future
	 * decompressions use more memory.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the block size is not in the
	 * permitted range.
	 */
	public BZip2WritableFileSettings setBlockSize(int blockSize) throws IllegalArgumentException
	{
		if ((blockSize < 1) || (blockSize > 9))
		{
			throw new IllegalArgumentException("" + blockSize);
		}
		m_blockSize = blockSize;
		return this;
	}

	public int getBlockSize()
	{
		return m_blockSize;
	}

	/**
	 * Set the buffer size used for the input buffer that may be used before
	 * compressing written data.
	 * <p>
	 * By default, no buffer is used.
	 * @param bufferSize The size of the memory buffer used for buffering the
	 * written output before it is compressed. Set this to {@code 0} to disable
	 * the buffer.
	 * @return {@code this}
	 * @throws IllegalArgumentException
	 */
	public BZip2WritableFileSettings setBufferSize(int bufferSize) throws IllegalArgumentException
	{
		if (bufferSize < 0)
		{
			throw new IllegalArgumentException("" + bufferSize);
		}
		m_bufferSize = bufferSize;
		return this;
	}

	public int getBufferSize()
	{
		return m_bufferSize;
	}

	/**
	 * Should the bzip2 implementation from Apache Commons Compress be used
	 * instead of At4J's?
	 * @param b Set this to {@code true} to use the bzip2 implementation from
	 * Commons Compress.
	 * @return {@code this}
	 */
	public BZip2WritableFileSettings setUseCommonsCompress(boolean b)
	{
		m_useCommonsCompress = b;
		return this;
	}

	public boolean isUseCommonsCompress()
	{
		return m_useCommonsCompress;
	}

	/**
	 * Set a {@link LogAdapter} to use for logging error and diagnostic output.
	 * This is only used for logging error output if several encoder threads are
	 * used.
	 * @param la The log adapter.
	 * @return {@code this}
	 */
	public BZip2WritableFileSettings setLogAdapter(LogAdapter la)
	{
		m_logAdapter = la;
		return this;
	}

	public LogAdapter getLogAdapter()
	{
		return m_logAdapter;
	}

	/**
	 * Set the number of encoder threads to use for encoding bzip2 blocks.
	 * <p>
	 * By default, bzip2 blocks are encoded in the thread that writes data to
	 * the writable file.
	 * <p>
	 * If an executor is set with the
	 * {@link #setExecutorService(BZip2EncoderExecutorService)} method, this
	 * property is ignored.
	 * @param no Number of encoder threads. Set this to {@code 0} to encode
	 * bzip2 blocks in the thread writing to the writable file.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the number is negative.
	 */
	public BZip2WritableFileSettings setNumberOfEncoderThreads(int no) throws IllegalArgumentException
	{
		if (no < 0)
		{
			throw new IllegalArgumentException("" + no);
		}
		m_numberOfEncoderThreads = no;
		return this;
	}

	public int getNumberOfEncoderThreads()
	{
		return m_numberOfEncoderThreads;
	}

	/**
	 * Set an executor service that will be used for spreading the work of
	 * encoding bzip2 blocks over several threads. The executor may be shared
	 * between several {@link BZip2WritableFile}:s and/or
	 * {@link BZip2OutputStream}:s.
	 * <p>
	 * An executor service is created using the
	 * {@link BZip2OutputStream#createExecutorService()} or the
	 * {@link BZip2OutputStream#createExecutorService(int)} method.
	 * @param executorService The executor service.
	 * @return {@code this}
	 */
	public BZip2WritableFileSettings setExecutorService(BZip2EncoderExecutorService executorService)
	{
		m_executorService = executorService;
		return this;
	}

	public BZip2EncoderExecutorService getExcutorService()
	{
		return m_executorService;
	}

	/**
	 * Set the number of tree refinement iterations that is run when creating
	 * Huffman trees for each compressed data block.
	 * <p>
	 * A higher value for this parameter should give better but slower
	 * compression. As the value increases the returns are diminishing.
	 * <p>
	 * The default value is five refinement iterations.
	 * @param no The number of Huffman tree refinement iterations. This should
	 * be a positive integer larger than zero.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the number is not a positive integer
	 * larger than zero.
	 */
	public BZip2WritableFileSettings setNumberOfHuffmanTreeRefinementIterations(int no) throws IllegalArgumentException
	{
		if (no < 1)
		{
			throw new IllegalArgumentException("Invalid value " + no + ". It must be greater than zero");
		}
		m_numberOfHuffmanTreeRefinementIterations = no;
		return this;
	}

	/**
	 * Get the number of Huffman tree refinement iterations.
	 * @return The number of Huffman tree refinement iterations.
	 */
	public int getNumberOfHuffmanTreeRefinementIterations()
	{
		return m_numberOfHuffmanTreeRefinementIterations;
	}

	/**
	 * Get the settings for a {@link BZip2OutputStream} based on the
	 * configuration of this object.
	 * @return Stream settings.
	 */
	public BZip2OutputStreamSettings getOutputStreamSettings()
	{
		BZip2OutputStreamSettings res = new BZip2OutputStreamSettings();
		res.setBlockSize(m_blockSize);
		res.setExecutorService(m_executorService);
		res.setLogAdapter(m_logAdapter);
		res.setNumberOfEncoderThreads(m_numberOfEncoderThreads);
		res.setNumberOfHuffmanTreeRefinementIterations(m_numberOfHuffmanTreeRefinementIterations);
		return res;
	}

	/**
	 * Make a copy of this object.
	 */
	@Override
	public BZip2WritableFileSettings clone()
	{
		try
		{
			return (BZip2WritableFileSettings) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new At4JException("Bug", e);
		}
	}
}
