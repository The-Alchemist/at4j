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
import java.io.IOException;

import org.at4j.comp.prog.IgnoreFileException;
import org.at4j.support.prog.CommandErrorException;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This runnable class emulates the {@code bzip2} command. It understands a
 * subset of {@code bzip2}'s command line arguments.
 * <p>
 * The program compresses one or several files. A file is compressed to a new
 * file with ".bz2" added to the name of the original file. The original file is
 * deleted after it has been successfully compressed.
 * 
 * <pre>
 * Usage:
 *   java -cp [classpath] org.at4j.comp.bzip2.prog.BZip2 [options] file[s]
 * Where:
 *   classpath - Should include the at4j, bzip2 and entityfs-core Jars.
 *   options   - Command options. See below.
 *   file[s]   - The files to compress.
 * Options:
 *   -1 (or --fast) to -9 (or --best)
 *             - Set the block size to 100 kb, 200 kb ... 900 kb when
 *               compressing. Default is --best.
 *   --apache  - Use Apache Commons Compress' bzip2 implementation instead of
 *               At4J's
 *   -c        - Write the compressed data to standard output and do not delete
 *               the original files.
 *   --decompress
 *   --uncompress
 *   -d        - Decompress files (instead of compressing them).
 *   --encoderThreads no - Set the number of encoder threads to use for each
 *               file. The default number of encoder threads is zero, which
 *               means that each file is encoded by the thread that writes it.
 *   --keep
 *   -k        - Don't delete the original files.
 *   --suffix suf
 *   -S suf    - Use the suffix &quot;suf&quot; instead of &quot;.bz2&quot; for the compressed files.
 *   --threads no - Set the maximum number of threads to use for compressing.
 *               Different files may be compressed in different threads. The
 *               default number of threads is one.
 *   --timing  - After building the archive, print out how long it took.
 * </pre>
 * @author Karl Gustafsson
 * @since 1.0
 * @see BUnzip2
 */
public final class BZip2 extends AbstractBZip2Program<BZip2CommandLineArguments>
{
	@Override
	protected BZip2CommandLineArguments parseCommandLine(String[] args) throws CommandErrorException
	{
		BZip2CommandLineArguments res = new BZip2CommandLineArguments();
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
			else if ("-d".equals(arg) || "--decompress".equals(arg) || "--uncompress".equals(arg))
			{
				res.setDecompress();
			}
			else if ("-k".equals(arg) || "--keep".equals(arg))
			{
				res.setDontDeleteOriginalFiles();
			}
			else if ("-S".equals(arg) || "--suffix".equals(arg))
			{
				res.setCompressedFileSuffix(getArg(args, pos++, "Missing compressed file suffix"));
			}
			else if ("--fast".equals(arg) || "-1".equals(arg))
			{
				res.setBlockSize(1);
			}
			else if ("-2".equals(arg))
			{
				res.setBlockSize(2);
			}
			else if ("-3".equals(arg))
			{
				res.setBlockSize(3);
			}
			else if ("-4".equals(arg))
			{
				res.setBlockSize(4);
			}
			else if ("-5".equals(arg))
			{
				res.setBlockSize(5);
			}
			else if ("-6".equals(arg))
			{
				res.setBlockSize(6);
			}
			else if ("-7".equals(arg))
			{
				res.setBlockSize(7);
			}
			else if ("-8".equals(arg))
			{
				res.setBlockSize(8);
			}
			else if ("--best".equals(arg) || "-9".equals(arg))
			{
				res.setBlockSize(9);
			}
			else if ("--apache".equals(arg))
			{
				res.setApache(true);
			}
			else if ("--encoderThreads".equals(arg))
			{
				res.setNumberOfEncoderThreads(Integer.parseInt(getArg(args, pos++, "Missing compressed file suffix")));
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
	String getTargetFileName(String sourceFileName, BZip2CommandLineArguments settings) throws IgnoreFileException
	{
		if (settings.isDecompress())
		{
			if (sourceFileName.toLowerCase().endsWith(".bz2"))
			{
				return sourceFileName.substring(0, sourceFileName.length() - 4);
			}
			else
			{
				throw new IgnoreFileException(sourceFileName + ": unknown suffix -- ignored");
			}
		}
		else
		{
			return sourceFileName + settings.getCompressedFileSuffix();
		}
	}

	public static void main(String[] args)
	{
		try
		{
			new BZip2().run(args);
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
