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
package org.at4j.comp.bzip2.prog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.at4j.comp.prog.StreamCompressionProgramArguments;

abstract class AbstractBZip2ProgramArguments extends StreamCompressionProgramArguments
{
	private final List<File> m_sources = new ArrayList<File>();
	private boolean m_compressToStdout;
	private boolean m_dontDeleteOriginalFiles;
	private boolean m_decompress = false;
	private boolean m_apache = false;
	private int m_blockSize = 9;
	private int m_numberOfEncoderThreads = 0;

	void setDecompress()
	{
		m_decompress = true;
	}

	boolean isDecompress()
	{
		return m_decompress;
	}

	void addSource(File source)
	{
		m_sources.add(source);
	}

	List<File> getSources()
	{
		return m_sources;
	}

	@Override
	public int getNumberOfSources()
	{
		return m_sources.size();
	}

	void setCompressToStdout()
	{
		m_compressToStdout = true;
	}

	boolean isCompressToStdout()
	{
		return m_compressToStdout;
	}

	void setDontDeleteOriginalFiles()
	{
		m_dontDeleteOriginalFiles = true;
	}

	boolean isDontDeleteOriginalFiles()
	{
		return m_dontDeleteOriginalFiles;
	}

	void setApache(boolean b)
	{
		m_apache = b;
	}

	boolean isApache()
	{
		return m_apache;
	}

	void setBlockSize(int sz)
	{
		m_blockSize = sz;
	}

	int getBlockSize()
	{
		return m_blockSize;
	}

	void setNumberOfEncoderThreads(int no)
	{
		if (no < 0)
		{
			throw new IllegalArgumentException("Invalid number of encoder threads: " + no);
		}
		m_numberOfEncoderThreads = no;
	}

	int getNumberOfEncoderThreads()
	{
		return m_numberOfEncoderThreads;
	}
}
