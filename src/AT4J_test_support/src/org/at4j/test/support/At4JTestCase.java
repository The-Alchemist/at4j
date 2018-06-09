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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.entityfs.Directory;
import org.entityfs.ReadWritableFile;
import org.entityfs.fs.FSRWFileSystemBuilder;
import org.entityfs.util.Entities;
import org.entityfs.util.cap.entity.ECFileResolvableUtil;
import org.entityfs.util.io.ReadWritableFileAdapter;

/**
 * Abstract base class for test cases.
 * @author Karl Gustafsson
 * @since 1.0
 */
public abstract class At4JTestCase
{
	// The Codepage 437 charset, a.k.a. DOS-US
	protected static final Charset CP437 = Charset.forName("cp437");

	// The Codepage 1252 charset, a.k.a. Windows Latin1
	protected static final Charset CP1252 = Charset.forName("cp1252");

	// The Mac roman charset.
	protected static final Charset MAC_ROMAN = Charset.forName("macroman");

	protected String getFromSystemProperty(String name)
	{
		String res = System.getProperty(name);
		if (res == null)
		{
			throw new RuntimeException("The system property " + name + " is not set");
		}
		return res;
	}

	protected Directory createTempDirectory()
	{
		try
		{
			File f = File.createTempFile("at4j", ".tmp");
			assertTrue(f.delete());
			assertTrue(f.mkdir());
			return new FSRWFileSystemBuilder().setRoot(f).enableLocking().create().getRootDirectory();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void deleteTempDirectory(Directory d)
	{
		Entities.deleteRecursively(d, false);
		d.getFileSystem().close();
		assertTrue(ECFileResolvableUtil.getFileObject(d).delete());
	}

	protected ReadWritableFile getTestDataFile(String relPath)
	{
		return new ReadWritableFileAdapter(new File(getFromSystemProperty("at4j.test.testDataPath") + File.separator + relPath));
	}

	protected ReadWritableFile getDocumentationFile(String relPath)
	{
		return new ReadWritableFileAdapter(new File(getFromSystemProperty("at4j.test.docPath") + File.separator + relPath));
	}

	protected Date getLocalDate(String s)
	{
		try
		{
			if (s.length() == 14)
			{
				return new SimpleDateFormat("yyyyMMddHHmmss").parse(s);
			}
			else if (s.length() == 17)
			{
				return new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(s);
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}
		catch (ParseException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected Date getUtcDate(String s)
	{
		try
		{
			if (s.length() == 14)
			{
				return new SimpleDateFormat("yyyyMMddHHmmss z").parse(s + " UTC");
			}
			else if (s.length() == 17)
			{
				return new SimpleDateFormat("yyyyMMddHHmmssSSS z").parse(s + " UTC");
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}
		catch (ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void assertWithinLast30Seconds(Date d)
	{
		long now = System.currentTimeMillis();
		long then = d.getTime();
		assertTrue(now >= then);
		assertTrue(now - then < 30000);
	}
}
