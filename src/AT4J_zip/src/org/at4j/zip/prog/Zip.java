package org.at4j.zip.prog;

import java.io.File;
import java.io.IOException;

import org.at4j.support.prog.AbstractProgram;
import org.at4j.support.prog.CommandErrorException;
import org.at4j.zip.builder.ZipBuilder;
import org.entityfs.RandomlyAccessibleFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.util.io.ReadWritableFileAdapter;

/**
 * This runnable class emulates the {@code zip} command. It understands a subset
 * of {@code zip}'s command line arguments.
 * <p>
 * 
 * <pre>
 * Usage:
 *   java -cp [classpath] org.at4j.zip.prog.Zip [options] file file[s]
 * Where:
 *   classpath - Should include the at4j, bzip, lzma and entityfs-core Jars.
 *   options   - Command options. See below.
 *   file      - The name and path of the Zip file to create.
 *   file[s]   - The files and directories to add to the Zip archive. If the
 *               -r option is used, directories are added recursively.
 * Options:
 *   -r        - Add directories recursively.
 *   --timing  - After building the archive, print out how long it took.
 * </pre>
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class Zip extends AbstractProgram
{
	private ZipCommandLineArguments parseCommandLine(String[] args) throws CommandErrorException
	{
		ZipCommandLineArguments res = new ZipCommandLineArguments();
		int pos = 0;
		String arg = getArg(args, pos++, "Missing files");
		while (isFlagArgument(arg))
		{
			if ("-r".equals(arg))
			{
				res.setRecursive();
			}
			else if ("--timing".equals(arg))
			{
				res.setTiming();
			}
			else
			{
				throw new CommandErrorException("Unknown argument " + arg);
			}
			arg = getArg(args, pos++, "Missing files");
		}
		res.setTargetFile(new File(arg));
		res.addFileToZip(new File(getArg(args, pos++, "Missing files to zip")));
		while (pos < args.length)
		{
			res.addFileToZip(new File(args[pos++]));
		}
		return res;
	}

	private void run(String[] args) throws CommandErrorException, IOException
	{
		ZipCommandLineArguments cla = parseCommandLine(args);

		long start = System.currentTimeMillis();

		File targetFile = cla.getTargetFile();
		if (!targetFile.createNewFile())
		{
			throw new CommandErrorException("Could not create target file " + targetFile);
		}
		RandomlyAccessibleFile f = new ReadWritableFileAdapter(targetFile);
		ZipBuilder zb = new ZipBuilder(f);
		try
		{
			for (File ftz : cla.getFilesToZip())
			{
				AbsoluteLocation parentLocation = getParentLocation(ftz);
				if (cla.isRecursive())
				{
					zb.addRecursively(ftz, parentLocation.getChildLocation(ftz.getName()));
				}
				else
				{
					zb.add(ftz, parentLocation);
				}
			}
		}
		finally
		{
			zb.close();
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
			new Zip().run(args);
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
