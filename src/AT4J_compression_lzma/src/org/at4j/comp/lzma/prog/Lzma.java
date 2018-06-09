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
import java.io.IOException;

import org.at4j.support.prog.CommandErrorException;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This runnable class emulates the {@code lzma} command. It understands a
 * subset of {@code lzma}'s command line arguments.
 * <p>
 * The program compresses one or several files. A file is compressed to a new
 * file with ".lzma" added to the name of the original file. The original file
 * is deleted after it has been successfully compressed.
 * 
 * <pre>
 * Usage:
 *   java -cp [classpath] org.at4j.comp.lzma.prog.Lzma [options] file[s]
 * Where:
 *   classpath - Should include the at4j, lzma and entityfs-core Jars.
 *   options   - Command options. See below.
 *   file[s]   - The files to compress.
 * Options:
 *   -c        - Write the compressed data to standard output and do not delete
 *               the original files.
 *   --decompress
 *   --uncompress
 *   -d        - Decompress files (instead of compressing them).
 *   --suffix suf
 *   -S suf    - Use the suffix &quot;suf&quot; instead of &quot;.gz&quot; for the compressed files.
 *   --timing  - After building the archive, print out how long it took.
 * </pre>
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnLzma
 */
public final class Lzma extends AbstractLzmaProgram<LzmaCommandLineArguments>
{
	@Override
	LzmaCommandLineArguments parseCommandLine(String[] args) throws CommandErrorException
	{
		LzmaCommandLineArguments res = new LzmaCommandLineArguments();
		int pos = 0;
		String arg = getArg(args, pos++, "Missing files");
		while (isFlagArgument(arg))
		{
			if ("--timing".equals(arg))
			{
				res.setTiming();
			}
			else if ("-c".equals(arg))
			{
				res.setWriteToStdout();
				res.setDontDeleteSourceFile();
			}
			else if ("-S".equals(arg) || "--suffix".equals(arg))
			{
				res.setCompressedFileSuffix(getArg(args, pos++, "Missing compressed file suffix"));
			}
			else if ("-d".equals(arg) || "--decompress".equals(arg) || "--uncompress".equals(arg))
			{
				res.setDecompress();
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
	boolean isDecompress(LzmaCommandLineArguments settings)
	{
		return settings.isDecompress();
	}

	public static void main(String[] args) throws InterruptedException
	{
		try
		{
			new Lzma().run(args);
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
