package org.at4j.zip.prog;

import java.io.File;

import org.at4j.support.prog.CommandErrorException;

final class UnzipCommandLineArguments
{
	private File m_zipFile;
	private boolean m_timing;
	private boolean m_quiet;

	void setZipFile(File f) throws CommandErrorException
	{
		if (!f.exists())
		{
			throw new CommandErrorException(f + " does not exist");
		}
		else if (!f.isFile())
		{
			throw new CommandErrorException(f + " is not a file");
		}
		m_zipFile = f;
	}

	File getZipFile()
	{
		return m_zipFile;
	}

	void setTiming()
	{
		m_timing = true;
	}

	boolean isTiming()
	{
		return m_timing;
	}

	void setQuiet()
	{
		m_quiet = true;
	}

	boolean isQuiet()
	{
		return m_quiet;
	}
}
