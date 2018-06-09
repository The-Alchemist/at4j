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
package org.at4j.tar.prog;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.entityfs.ostrat.DoOverwriteAndLogWarning;
import org.entityfs.ostrat.OverwriteStrategy;

final class TarCommandLineArguments
{
	private TarCommand m_command;
	private boolean m_gzip;
	private boolean m_bzip2;
	private boolean m_lzma;
	private File m_tarFile;
	private List<File> m_filesToTar = new ArrayList<File>();
	private boolean m_timing;
	private Charset m_charset = Charset.defaultCharset();
	private OverwriteStrategy m_overwriteStrategy = DoOverwriteAndLogWarning.INSTANCE;
	private String m_archiveFormat = "gnu";

	void setCommand(TarCommand cmd)
	{
		m_command = cmd;
	}

	TarCommand getCommand()
	{
		return m_command;
	}

	boolean isGZip()
	{
		return m_gzip;
	}

	void setGZip()
	{
		m_gzip = true;
	}

	boolean isBZip2()
	{
		return m_bzip2;
	}

	void setBZip2()
	{
		m_bzip2 = true;
	}

	boolean isLzma()
	{
		return m_lzma;
	}

	void setLzma()
	{
		m_lzma = true;
	}

	void setTarFile(File f)
	{
		m_tarFile = f;
	}

	File getTarFile()
	{
		return m_tarFile;
	}

	void addFileToTar(File f)
	{
		m_filesToTar.add(f);
	}

	List<File> getFilesToTar()
	{
		return m_filesToTar;
	}

	void setTiming()
	{
		m_timing = true;
	}

	boolean isTiming()
	{
		return m_timing;
	}

	void setCharset(Charset cs)
	{
		m_charset = cs;
	}

	Charset getCharset()
	{
		return m_charset;
	}

	void setOverwriteStrategy(OverwriteStrategy strat)
	{
		m_overwriteStrategy = strat;
	}

	OverwriteStrategy getOverwriteStrategy()
	{
		return m_overwriteStrategy;
	}

	void setArchiveFormat(String format)
	{
		m_archiveFormat = format;
	}

	String getArchiveFormat()
	{
		return m_archiveFormat;
	}
}
