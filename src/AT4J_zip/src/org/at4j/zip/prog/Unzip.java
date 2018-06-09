package org.at4j.zip.prog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.at4j.support.prog.AbstractProgram;
import org.at4j.support.prog.CommandErrorException;
import org.at4j.zip.ZipDirectoryEntry;
import org.at4j.zip.ZipEntry;
import org.at4j.zip.ZipFile;
import org.at4j.zip.ZipFileEntry;
import org.entityfs.support.exception.WrappedIOException;
import org.entityfs.support.io.StreamUtil;
import org.entityfs.util.io.ReadWritableFileAdapter;

/**
 * This runnable class emulates the {@code unzip} command. It understands a
 * subset of {@code unzip}'s command line arguments.
 * <p>
 * 
 * <pre>
 * Usage:
 *   java -cp [classpath] org.at4j.zip.prog.Unzip [options] file
 * Where:
 *   classpath - Should include the at4j, bzip, lzma and entityfs-core Jars.
 *   options   - Unzip options (see below)
 *   file      - The name and path of the Zip file to unzip.
 * Options:
 *    -q       - Quiet.
 *   --timing  - After extracting the files, print out how long it took.
 * </pre>
 * <p>
 * Files are unpacked starting in the current working directory.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class Unzip extends AbstractProgram
{
	private UnzipCommandLineArguments parseCommandLine(String[] args) throws CommandErrorException
	{
		UnzipCommandLineArguments res = new UnzipCommandLineArguments();
		int pos = 0;
		String arg = getArg(args, pos++, "Missing file");
		while (isFlagArgument(arg))
		{
			if ("--timing".equals(arg))
			{
				res.setTiming();
			}
			else if ("-q".equals(arg))
			{
				res.setQuiet();
			}
			else
			{
				throw new CommandErrorException("Unknown argument " + arg);
			}
			arg = getArg(args, pos++, "Missing files");
		}
		res.setZipFile(new File(arg));
		if (pos < args.length)
		{
			throw new CommandErrorException("Cannot handle files argument");
		}
		return res;
	}

	private void extractFile(ZipFileEntry zfe, UnzipCommandLineArguments settings) throws CommandErrorException, IOException
	{
		String location = zfe.getLocation().getLocation().substring(1);
		if (!settings.isQuiet())
		{
			System.out.println(location);
		}
		File f = new File(location);
		if (!f.createNewFile())
		{
			throw new CommandErrorException("Could not create file " + f);
		}
		InputStream is = new BufferedInputStream(zfe.openForRead());
		try
		{
			OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
			try
			{
				StreamUtil.copyStreams(is, os, 4096, zfe.getDataSize());
			}
			catch (WrappedIOException e)
			{
				throw e.getWrapped();
			}
			finally
			{
				os.close();
			}
		}
		finally
		{
			is.close();
		}
		if (!f.setLastModified(zfe.getLastModified().getTime()))
		{
			System.err.println("Could not set last modification time for file " + f);
		}
	}

	private void extractDirectory(ZipDirectoryEntry zde, UnzipCommandLineArguments settings) throws CommandErrorException, IOException
	{
		String location = zde.getLocation().getLocation().substring(1);
		if (!settings.isQuiet())
		{
			System.out.println(location);
		}
		File d = new File(location);
		if (!("".equals(location) || d.mkdir()))
		{
			throw new CommandErrorException("Could not create directory " + d);
		}
		for (ZipEntry ze : zde.getChildEntries().values())
		{
			if (ze instanceof ZipDirectoryEntry)
			{
				extractDirectory((ZipDirectoryEntry) ze, settings);
			}
			else if (ze instanceof ZipFileEntry)
			{
				extractFile((ZipFileEntry) ze, settings);
			}
			else
			{
				throw new CommandErrorException("Unknown entry type " + ze);
			}
		}
		if (!d.setLastModified(zde.getLastModified().getTime()))
		{
			System.err.println("Could not set last modification time for directory " + d);
		}
	}

	private void run(String[] args) throws CommandErrorException, IOException
	{
		UnzipCommandLineArguments cla = parseCommandLine(args);

		long start = System.currentTimeMillis();

		ZipFile zf = new ZipFile(new ReadWritableFileAdapter(cla.getZipFile()));
		try
		{
			extractDirectory(zf.getRootEntry(), cla);
		}
		finally
		{
			zf.close();
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
			new Unzip().run(args);
		}
		catch (CommandErrorException e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
