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
package org.at4j.comp.lzma.prog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractLzmaCommandLineArguments
{
	private final List<File> m_sources = new ArrayList<File>();
	private boolean m_timing;
	private boolean m_writeToStdout;
	private boolean m_dontDeleteSourceFile;
	private String m_compressedFileSuffix = ".lzma";

	void addSource(File s)
	{
		m_sources.add(s);
	}

	List<File> getSources()
	{
		return m_sources;
	}

	void setTiming()
	{
		m_timing = true;
	}

	boolean isTiming()
	{
		return m_timing;
	}

	void setWriteToStdout()
	{
		m_writeToStdout = true;
	}

	boolean isWriteToStdout()
	{
		return m_writeToStdout;
	}

	void setDontDeleteSourceFile()
	{
		m_dontDeleteSourceFile = true;
	}

	boolean isDontDeleteSourceFile()
	{
		return m_dontDeleteSourceFile;
	}

	void setCompressedFileSuffix(String s)
	{
		m_compressedFileSuffix = s;
	}

	String getCompressedFileSuffix()
	{
		return m_compressedFileSuffix;
	}
}
