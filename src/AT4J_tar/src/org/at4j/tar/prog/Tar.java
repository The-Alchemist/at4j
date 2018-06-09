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
import java.io.IOException;
import java.nio.charset.Charset;

import org.at4j.comp.bzip2.BZip2ReadableFile;
import org.at4j.comp.bzip2.BZip2ReadableFileSettings;
import org.at4j.comp.bzip2.BZip2WritableFile;
import org.at4j.comp.bzip2.BZip2WritableFileSettings;
import org.at4j.comp.lzma.LzmaReadableFile;
import org.at4j.comp.lzma.LzmaWritableFile;
import org.at4j.support.prog.AbstractProgram;
import org.at4j.support.prog.CommandErrorException;
import org.at4j.tar.PrintTarEntryExtractionStrategy;
import org.at4j.tar.TarExtractSpecification;
import org.at4j.tar.TarExtractor;
import org.at4j.tar.builder.GnuTarEntryStrategy;
import org.at4j.tar.builder.PaxTarEntryStrategy;
import org.at4j.tar.builder.TarBuilderSettings;
import org.at4j.tar.builder.TarStreamBuilder;
import org.at4j.tar.builder.UstarEntryStrategy;
import org.at4j.tar.builder.V7TarEntryStrategy;
import org.entityfs.Directory;
import org.entityfs.DirectoryView;
import org.entityfs.ReadableFile;
import org.entityfs.WritableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.ostrat.DontOverwriteAndLogWarning;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.util.FileReadableFile;
import org.entityfs.util.io.GZipReadableFile;
import org.entityfs.util.io.GZipWritableFile;
import org.entityfs.util.io.ReadWritableFileAdapter;

/**
 * This runnable class emulates the {@code tar} command. It understands a subset
 * of {@code tar}'s command line arguments.
 * <p>
 * 
 * <pre>
 * Usage:
 *   java -cp [classpath] org.at4j.tar.prog.Tar [-] command [options] pathname
 *   [pathname ...]
 * Where:
 *   classpath - Should include the at4j, bzip, lzma, entityfs-core and
 *               entityfs-util Jars.
 *   command   - The Tar command. See below.
 *   options   - Command options. See below.
 *   file      - The name and path of the Zip file to unzip.
 * Commands:
 *   -c
 *   --create  - Create an archive. This requires the &quot;f&quot; option.
 *   -t
 *   --list    - List the contents of the archive. This requires the &quot;f&quot; option.
 *   -x
 *   --extract - Extract an archive. This requires the &quot;f&quot; option.
 * Options:
 *   --charset name
 *             - The name of the charset to use for encoding or decoding file
 *               names in the Tar file. If this is not set, the platform's
 *               default charset is used.
 *   -f
 *   --file f  - The name and path of the Tar file.
 *   -H format
 *   --format=format
 *             - (when creating an archive) Create an archive using the 
 *               specified format, where the format is one of the following:
 *                 'v7':    Unix V7 Tar format
 *                 'gnu':   Gnu Tar format (default)
 *                 'ustar': Posix.1-1988 compatible archive
 *                 'posix': Posix.1-2001 compatible archive
 *   -j         
 *   --bzip2   - The Tar file is bzipped.
 *   -k
 *   --keep-old-files
 *             - Don't overwrite existing files when extracting an archive.
 *   --lzma    - The Tar file is compressed with LZMA compression.
 *   --timing  - After extracting the files, print out how long it took.
 *   -z
 *   --gzip    - The Tar file is gzipped.
 * </pre>
 * <p>
 * The options can be used in the old {@code tar} condensed style does. For
 * instance, {@code xfz foo.tar.gz} means
 * "extract the gzipped Tar file foo.tar.gz".
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class Tar extends AbstractProgram
{
	private boolean parseCondensedCommand(String cmd, TarCommandLineArguments cla) throws CommandErrorException
	{
		char command = cmd.charAt(0);
		if (command == 'x')
		{
			cla.setCommand(TarCommand.EXTRACT);
		}
		else if (command == 't')
		{
			cla.setCommand(TarCommand.LIST);
		}
		else if (command == 'c')
		{
			cla.setCommand(TarCommand.CREATE);
		}
		else
		{
			throw new CommandErrorException("Unknown command: " + command);
		}

		boolean nextArgumentIsFile = false;
		for (int i = 1; i < cmd.length(); i++)
		{
			char option = cmd.charAt(i);
			if (option == 'f')
			{
				nextArgumentIsFile = true;
			}
			else if (option == 'z')
			{
				cla.setGZip();
			}
			else if (option == 'j')
			{
				cla.setBZip2();
			}
			else
			{
				throw new CommandErrorException("Unknown Tar option: " + option);
			}
		}
		return nextArgumentIsFile;
	}

	private void verifyAllMandatorySettingsPresent(TarCommandLineArguments cla) throws CommandErrorException
	{
		if (cla.getTarFile() == null)
		{
			throw new CommandErrorException("Missing Tar file");
		}

		if (cla.getFilesToTar().isEmpty())
		{
			if (cla.getCommand() == TarCommand.CREATE)
			{
				throw new CommandErrorException("Missing files to add to archive");
			}
		}
		else
		{
			if (cla.getCommand() != TarCommand.CREATE)
			{
				throw new CommandErrorException("Don't know what to do with " + cla.getTarFile());
			}
		}
	}

	private int parseOption(String option, String[] args, int nextPos, TarCommandLineArguments cla) throws CommandErrorException
	{
		if ("-f".equals(option) || "--file".equals(option))
		{
			cla.setTarFile(new File(getArg(args, nextPos, "Missing Tar file")));
			return 1;
		}
		else if ("-z".equals(option) || "--gzip".equals(option))
		{
			cla.setGZip();
			return 0;
		}
		else if ("-j".equals(option) || "--bzip2".equals(option))
		{
			cla.setBZip2();
			return 0;
		}
		else if ("--lzma".equals(option))
		{
			cla.setLzma();
			return 0;
		}
		else if ("--timing".equals(option))
		{
			cla.setTiming();
			return 0;
		}
		else if ("--charset".equals(option))
		{
			cla.setCharset(Charset.forName(getArg(args, nextPos, "Missing charset name")));
			return 1;
		}
		else if (("-k".equals(option)) || (("--keep-old-files").equals(option)))
		{
			cla.setOverwriteStrategy(DontOverwriteAndLogWarning.INSTANCE);
			return 0;
		}
		else if (option.startsWith("--format="))
		{
			cla.setArchiveFormat(option.substring(9));
			return 0;
		}
		else if ("-H".equals(option))
		{
			cla.setArchiveFormat(getArg(args, nextPos, "Missing archive format"));
			return 1;
		}
		else
		{
			throw new CommandErrorException("Unknown option: " + option);
		}
	}

	private TarCommandLineArguments parseCommandLine(String[] args) throws CommandErrorException
	{
		TarCommandLineArguments cla = new TarCommandLineArguments();
		int pos = 0;
		String arg = getArg(args, pos++, "Missing command");
		if (!(arg.charAt(0) == '-'))
		{
			boolean nextArgumentIsFile = parseCondensedCommand(arg, cla);
			if (nextArgumentIsFile)
			{
				arg = getArg(args, pos++, "Missing file");
				cla.setTarFile(new File(arg));
			}
		}
		else if ("-c".equals(arg))
		{
			cla.setCommand(TarCommand.CREATE);
		}
		else if ("-t".equals(arg))
		{
			cla.setCommand(TarCommand.LIST);
		}
		else if ("-x".equals(arg))
		{
			cla.setCommand(TarCommand.EXTRACT);
		}
		else
		{
			throw new CommandErrorException("Unknown command: " + arg);
		}

		boolean parsingOptions = true;
		while (pos < args.length)
		{
			arg = args[pos++];
			if (parsingOptions)
			{
				if (arg.startsWith("-"))
				{
					pos += parseOption(arg, args, pos, cla);
				}
				else
				{
					parsingOptions = false;
				}
			}

			if (!parsingOptions)
			{
				cla.addFileToTar(new File(arg));
			}
		}

		verifyAllMandatorySettingsPresent(cla);
		return cla;
	}

	private void create(TarCommandLineArguments cla) throws CommandErrorException, IOException
	{
		File targetFile = cla.getTarFile();
		if (targetFile.exists() && (!targetFile.delete()))
		{
			throw new CommandErrorException("Could not delete " + targetFile);
		}
		if (!targetFile.createNewFile())
		{
			throw new CommandErrorException("Could not create target file " + targetFile);
		}

		WritableFile f = new ReadWritableFileAdapter(targetFile);
		if (cla.isGZip())
		{
			f = new GZipWritableFile(f);
		}
		else if (cla.isBZip2())
		{
			f = new BZip2WritableFile(f, new BZip2WritableFileSettings().setBlockSize(9));
		}
		else if (cla.isLzma())
		{
			f = new LzmaWritableFile(f);
		}

		TarBuilderSettings tbs = new TarBuilderSettings();
		if ("gnu".equals(cla.getArchiveFormat()))
		{
			tbs.setEntryStrategy(new GnuTarEntryStrategy(cla.getCharset()));
		}
		else if ("v7".equals(cla.getArchiveFormat()))
		{
			tbs.setEntryStrategy(new V7TarEntryStrategy(cla.getCharset()));
		}
		else if ("ustar".equals(cla.getArchiveFormat()))
		{
			tbs.setEntryStrategy(new UstarEntryStrategy(cla.getCharset()));
		}
		else if ("posix".equals(cla.getArchiveFormat()))
		{
			tbs.setEntryStrategy(new PaxTarEntryStrategy(cla.getCharset()));
		}
		else
		{
			throw new CommandErrorException("Unknown archive format: " + cla.getArchiveFormat());
		}

		TarStreamBuilder tb = new TarStreamBuilder(f, tbs);
		try
		{
			for (File ftz : cla.getFilesToTar())
			{
				AbsoluteLocation parentLocation = getParentLocation(ftz);
				tb.addRecursively(ftz, parentLocation.getChildLocation(ftz.getName()));
			}
		}
		finally
		{
			tb.close();
		}
	}

	private ReadableFile getTarFile(TarCommandLineArguments cla) throws CommandErrorException
	{
		File tarFile = cla.getTarFile();
		if (!tarFile.exists())
		{
			throw new CommandErrorException(tarFile + " does not exist");
		}
		else if (!tarFile.isFile())
		{
			throw new CommandErrorException(tarFile + " is not a file");
		}

		ReadableFile res = new FileReadableFile(tarFile);
		if (cla.isGZip())
		{
			res = new GZipReadableFile(res);
		}
		else if (cla.isBZip2())
		{
			res = new BZip2ReadableFile(res, new BZip2ReadableFileSettings().setBufferSize(4096));
		}
		else if (cla.isLzma())
		{
			res = new LzmaReadableFile(res);
		}
		return res;
	}

	private void extract(TarCommandLineArguments cla) throws CommandErrorException, IOException
	{
		// Extract the file in the current working directory
		Directory target = new FSRWFileSystemBuilder().disableAccessControls().disableEntityValidityControls().setRoot(new File(".")).create().getRootDirectory();
		ReadableFile f = getTarFile(cla);
		TarExtractSpecification spec = new TarExtractSpecification();
		spec.setFileNameCharset(cla.getCharset());
		spec.setOverwriteStrategy(cla.getOverwriteStrategy());
		new TarExtractor(f).extract(target, spec);
	}

	private void list(TarCommandLineArguments cla) throws CommandErrorException, IOException
	{
		ReadableFile f = getTarFile(cla);
		TarExtractSpecification spec = new TarExtractSpecification();
		spec.setFileNameCharset(cla.getCharset());
		spec.setEntryExtractionStrategy(new PrintTarEntryExtractionStrategy(System.out));
		new TarExtractor(f).extract((DirectoryView) null, spec);
	}

	private void run(String[] args) throws CommandErrorException, IOException
	{
		TarCommandLineArguments cla = parseCommandLine(args);

		long start = System.currentTimeMillis();

		switch (cla.getCommand())
		{
			case EXTRACT:
				extract(cla);
				break;
			case CREATE:
				create(cla);
				break;
			case LIST:
				list(cla);
				break;
			default:
				throw new CommandErrorException("Bug: unknown command " + cla.getCommand());
		}

		long end = System.currentTimeMillis();
		if (cla.isTiming())
		{
			System.out.println("The operation took " + (end - start) + " ms.");
			System.out.println("The time it took to load Java and start the program is not included.");
		}
	}

	public static void main(String[] args)
	{
		try
		{
			new Tar().run(args);
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
