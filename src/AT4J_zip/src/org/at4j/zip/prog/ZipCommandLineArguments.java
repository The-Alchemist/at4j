package org.at4j.zip.prog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.at4j.support.prog.CommandErrorException;

final class ZipCommandLineArguments
{
	private boolean m_recursive;
	private boolean m_timing;
	private File m_targetFile;
	private final List<File> m_filesToZip = new ArrayList<File>();

	void setRecursive()
	{
		m_recursive = true;
	}

	boolean isRecursive()
	{
		return m_recursive;
	}

	void setTargetFile(File f) throws CommandErrorException
	{
		if (f.exists())
		{
			throw new CommandErrorException(f + " already exists");
		}
		m_targetFile = f;
	}

	File getTargetFile()
	{
		return m_targetFile;
	}

	void addFileToZip(File f) throws CommandErrorException
	{
		if (!f.exists())
		{
			throw new CommandErrorException(f + " does not exist");
		}
		m_filesToZip.add(f);
	}

	List<File> getFilesToZip()
	{
		return m_filesToZip;
	}

	void setTiming()
	{
		m_timing = true;
	}

	boolean isTiming()
	{
		return m_timing;
	}
}
