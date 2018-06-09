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
package org.at4j.comp.gzip.prog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.at4j.comp.prog.AbstractStreamCompressionProgram;
import org.at4j.comp.prog.IgnoreFileException;

/**
 * Abstract base class for gzip compression programs.
 * @author Karl Gustafsson
 * @since 1.0
 */
abstract class AbstractGZipProgram<T extends AbstractGZipProgramArguments> extends AbstractStreamCompressionProgram<T>
{
	@Override
	protected InputStream createInputStream(int sourceNo, T settings) throws IOException
	{
		InputStream is = null;
		boolean successful = false;
		try
		{
			is = new FileInputStream(settings.getSources().get(sourceNo));
			is = new BufferedInputStream(is);
			if (settings.isDecompress())
			{
				is = new GZIPInputStream(is);
			}
			successful = true;
			return is;
		}
		finally
		{
			if ((!successful) && (is != null))
			{
				is.close();
			}
		}
	}

	abstract String getTargetFileName(String sourceFileName, T settings) throws IgnoreFileException;

	@Override
	protected OutputStream createOutputStream(int targetNo, T settings) throws IOException, IgnoreFileException
	{
		OutputStream os = null;
		boolean successful = false;
		try
		{
			if (settings.isCompressToStdout())
			{
				os = System.out;
			}
			else
			{
				File sourceFile = settings.getSources().get(targetNo);
				File targetFile = new File(sourceFile.getParentFile(), getTargetFileName(sourceFile.getName(), settings));
				if (targetFile.exists())
				{
					throw new IgnoreFileException(targetFile + " already exists");
				}
				os = new FileOutputStream(targetFile);
				os = new BufferedOutputStream(os);
			}
			if (!settings.isDecompress())
			{
				os = new GZIPOutputStream(os);
			}
			successful = true;
			return os;
		}
		finally
		{
			if ((!successful) && (os != null))
			{
				os.close();
			}
		}
	}

	@Override
	protected void postProcess(int sourceNo, T settings) throws IOException
	{
		if (!settings.isDontDeleteOriginalFiles())
		{
			File sourceFile = settings.getSources().get(sourceNo);
			if (!sourceFile.delete())
			{
				throw new IOException("Could not delete " + sourceFile + ". No reason given");
			}
		}
	}
}
