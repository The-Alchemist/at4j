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
package org.at4j.test.support;

import java.io.File;
import java.io.IOException;

import org.entityfs.Directory;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.util.cap.entity.ECFileResolvableUtil;

/**
 * This class contains static methods for working with test files.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class TestFileSupport
{
	// Hidden constructor
	private TestFileSupport()
	{
		// Nothing
	}

	public static File createTemporaryFile()
	{
		try
		{
			return File.createTempFile("at4jtest", ".tmp");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static File createTemporaryDir()
	{
		File f = createTemporaryFile();
		if (!f.delete())
		{
			throw new RuntimeException("Could not delete " + f + ". No reason given");
		}
		if (!f.mkdir())
		{
			throw new RuntimeException("Could not create directory " + f + ". No reason given");
		}
		return f;
	}

	public static Directory createTemporaryDirectory()
	{
		return new FSRWFileSystemBuilder().disableAccessControls().disableEntityValidityControls().setRoot(createTemporaryDir()).create().getRootDirectory();
	}

	public static void deleteRecursively(Directory d)
	{
		deleteRecursively(ECFileResolvableUtil.getFileObject(d));
	}

	public static void deleteRecursively(File f)
	{
		if (!f.exists())
		{
			return;
		}
		else
		{
			if (f.isDirectory())
			{
				for (File subf : f.listFiles())
				{
					deleteRecursively(subf);
				}
			}

			if (!f.delete())
			{
				// Sleep a couple of seconds and then try again
				try
				{
					Thread.sleep(3400);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				
				if (!f.delete())
				{
					// Don't throw a new exception here, since that may override
					// an exception that lead to this error.
					System.err.println("Could not delete " + f);
					f.deleteOnExit();
				}
			}
		}
	}
}
