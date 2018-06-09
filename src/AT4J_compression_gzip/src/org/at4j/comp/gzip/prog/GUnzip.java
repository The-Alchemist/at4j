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

import java.io.File;
import java.io.IOException;

import org.at4j.comp.prog.IgnoreFileException;
import org.at4j.support.prog.CommandErrorException;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This runnable class emulates the {@code gunzip} command. It understands a
 * subset of {@code gunzip}'s command line arguments.
 * <p>
 * The program decompresses one or several files. A file must have the file name
 * extension .gz to be decompressed. When it is decompressed, the .gz extension
 * is removed.
 * 
 * <pre>
 * Usage:
 *   java -cp [classpath] org.at4j.comp.gzip.prog.GUnzip [options] file[s]
 * Where:
 *   classpath - Should include the at4j and entityfs-core Jars.
 *   options   - Command options. See below.
 *   file[s]   - The files to compress.
 * Options:
 *   -c        - Write the decompressed data to standard output and do not delete
 *               the original files.
 *   --threads no - Set the maximum number of threads to use for compressing.
 *               Different files may be compressed in different threads. The
 *               default number of threads is 1.
 *   --timing  - After building the archive, print out how long it took.
 * </pre>
 * @author Karl Gustafsson
 * @since 1.0
 * @see GZip
 */
public final class GUnzip extends AbstractGZipProgram<GUnzipCommandLineArguments>
{
	@Override
	protected GUnzipCommandLineArguments parseCommandLine(String[] args) throws CommandErrorException
	{
		GUnzipCommandLineArguments res = new GUnzipCommandLineArguments();
		int pos = 0;
		String arg = getArg(args, pos++, "Missing files");
		while (isFlagArgument(arg))
		{
			if ("--threads".equals(arg))
			{
				int numberOfThreads = Integer.valueOf(getArg(args, pos++, "Missing number of threads"));
				res.setNumberOfThreads(numberOfThreads);
			}
			else if ("--timing".equals(arg))
			{
				res.setTiming();
			}
			else if ("-c".equals(arg))
			{
				res.setCompressToStdout();
				res.setDontDeleteOriginalFiles();
			}
			else
			{
				throw new CommandErrorException("Unknown argument " + arg);
			}
			arg = getArg(args, pos++, "Missing files");
		}
		res.addSource(new File(arg));
		while (pos < args.length)
		{
			arg = args[pos++];
			res.addSource(new File(arg));
		}
		return res;
	}

	@Override
	String getTargetFileName(String sourceFileName, GUnzipCommandLineArguments settings) throws IgnoreFileException
	{
		if (sourceFileName.toLowerCase().endsWith(".gz"))
		{
			return sourceFileName.substring(0, sourceFileName.length() - 3);
		}
		else
		{
			throw new IgnoreFileException(sourceFileName + ": unknown suffix -- ignored");
		}
	}

	public static void main(String[] args)
	{
		try
		{
			new GUnzip().run(args);
		}
		catch (CommandErrorException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch (WrappedIOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
