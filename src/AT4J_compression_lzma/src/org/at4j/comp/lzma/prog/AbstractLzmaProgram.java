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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.at4j.comp.prog.IgnoreFileException;
import org.at4j.support.lang.SignedLong;
import org.at4j.support.lang.UnsignedLong;
import org.at4j.support.prog.AbstractProgram;
import org.at4j.support.prog.CommandErrorException;

import SevenZip.Compression.LZMA.Decoder;
import SevenZip.Compression.LZMA.Encoder;

abstract class AbstractLzmaProgram<T extends AbstractLzmaCommandLineArguments> extends AbstractProgram
{
	private static final int STREAM_PROPERTIES_SIZE = 5; // bytes

	private File getTargetFile(File source, T settings) throws IgnoreFileException
	{
		String sourceName = source.getName();
		String suffix = settings.getCompressedFileSuffix();
		if (!sourceName.toLowerCase().endsWith(suffix))
		{
			throw new IgnoreFileException(sourceName + " does not end with " + suffix);
		}
		else
		{
			return new File(source.getParentFile(), sourceName.substring(0, sourceName.length() - suffix.length()));
		}
	}

	private void deleteSource(File source, T settings)
	{
		if (!settings.isDontDeleteSourceFile())
		{
			if (!source.delete())
			{
				System.err.println("Could not delete " + source + ". No reason given.");
			}
		}
	}

	private void decompress(T settings) throws CommandErrorException, IOException, InterruptedException
	{
		for (File source : settings.getSources())
		{
			try
			{
				File target = getTargetFile(source, settings);
				if (target.exists())
				{
					throw new IgnoreFileException(target + " already exists");
				}

				InputStream in = new BufferedInputStream(new FileInputStream(source));
				try
				{
					OutputStream out;
					if (settings.isWriteToStdout())
					{
						out = System.out;
					}
					else
					{
						out = new BufferedOutputStream(new FileOutputStream(target));
					}
					try
					{
						byte[] properties = new byte[STREAM_PROPERTIES_SIZE];

						int noRead = in.read(properties);
						if (noRead != STREAM_PROPERTIES_SIZE)
						{
							throw new IOException("Wanted to read " + STREAM_PROPERTIES_SIZE + " bytes. Got " + noRead);
						}

						long size = UnsignedLong.readBigEndian(in).longValue();

						Decoder dec = new Decoder();
						if (!dec.SetDecoderProperties(properties, size))
						{
							throw new IOException("Could not set LZMA decoder properties from the data in the supplied stream. No reason given.");
						}
						if (!dec.Code(in, out, size))
						{
							throw new IOException("Could not decode " + source + " No reason given.");
						}
					}
					finally
					{
						out.close();
					}
				}
				finally
				{
					in.close();
				}

				deleteSource(source, settings);
			}
			catch (IgnoreFileException e)
			{
				System.err.println("Ignoring " + source + ": " + e.getMessage());
			}
		}
	}

	private void compress(T settings) throws CommandErrorException, IOException, InterruptedException
	{
		for (File source : settings.getSources())
		{
			File target = new File(source.getParentFile(), source.getName() + settings.getCompressedFileSuffix());
			InputStream in = new BufferedInputStream(new FileInputStream(source));
			try
			{
				OutputStream out;
				if (settings.isWriteToStdout())
				{
					out = System.out;
				}
				else
				{
					out = new BufferedOutputStream(new FileOutputStream(target));
				}
				try
				{
					Encoder enc = new Encoder();
					enc.WriteCoderProperties(out);
					out.write(SignedLong.valueOf(source.length()).getBigEndianByteArray());
					enc.Code(in, out, -1, -1, null);
				}
				finally
				{
					out.close();
				}
			}
			finally
			{
				in.close();
			}

			deleteSource(source, settings);
		}
	}

	abstract T parseCommandLine(String[] args) throws CommandErrorException;

	abstract boolean isDecompress(T settings);

	void run(String[] args) throws CommandErrorException, IOException, InterruptedException
	{
		T settings = parseCommandLine(args);

		long start = System.currentTimeMillis();

		if (isDecompress(settings))
		{
			decompress(settings);
		}
		else
		{
			compress(settings);
		}

		long end = System.currentTimeMillis();
		if (settings.isTiming())
		{
			System.out.println("The operation took " + (end - start) + " ms.");
			System.out.println("The time it took to load Java and start the program is not included.");
		}
	}
}
