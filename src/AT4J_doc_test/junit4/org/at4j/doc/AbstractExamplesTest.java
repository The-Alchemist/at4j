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
package org.at4j.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.ToolProvider;

import org.at4j.test.support.At4JTestCase;
import org.entityfs.Directory;
import org.entityfs.support.exception.FileSystemException;
import org.entityfs.support.nio.Charsets;
import org.entityfs.support.util.TwoObjects;
import org.entityfs.util.FileSystems;
import org.entityfs.util.Files;
import org.entityfs.util.IteratorDeleter;
import org.entityfs.util.lang.EntityClassLoader;
import org.junit.Test;

/**
 * This class was copy-pasted from EntityFS documentation tests and then
 * modified for the At4J project.
 * @author Karl Gustafsson
 * @since 1.0
 */
public abstract class AbstractExamplesTest extends At4JTestCase
{
	private static final String METHOD_TEST_CODE_PREFIX = "public class At4JDocTest {";
	private static final String METHOD_TEST_METHOD_PREFIX = "public Object runTest(Object[] args) throws Exception {";
	private static final String METHOD_TEST_CODE_SUFFIX = "}}";

	private static final String CLASS_TEST_CODE_PREFIX = "public class At4JDocTest {";
	private static final String CLASS_TEST_CODE_SUFFIX = "}";

	private static final List<TwoObjects<String, String>> DEFAULT_REPLACES = new ArrayList<TwoObjects<String, String>>();

	static
	{
		// Add replaces for HTML entities
		DEFAULT_REPLACES.add(new TwoObjects<String, String>("&lt;", "<"));
		DEFAULT_REPLACES.add(new TwoObjects<String, String>("&gt;", ">"));
		DEFAULT_REPLACES.add(new TwoObjects<String, String>("&amp;", "&"));
	}

	protected String escapeBackslashes(String s)
	{
		// Replace one backslash with four, needed when inserting backspaced
		// strings into scripts. The rest is escaping...
		return s.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
	}

	private String runReplaces(String s, List<TwoObjects<String, String>> replaces)
	{
		if (replaces == null)
		{
			replaces = new ArrayList<TwoObjects<String, String>>();
		}

		replaces.addAll(0, DEFAULT_REPLACES);
		for (TwoObjects<String, String> rep : replaces)
		{
			s = s.replaceAll("\\Q" + rep.getFirst() + "\\E", rep.getSecond());
		}
		return s;
	}

	private Object runExampleTest(String s, Object[] args) throws IOException
	{
		File targetD = File.createTempFile("entityfstest", "tmp");
		assertTrue(targetD.delete());
		assertTrue(targetD.mkdir());
		Directory targetDir = FileSystems.getEntityForDirectory(targetD, false);
		try
		{
			JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
			if (jc == null)
			{
				throw new FileSystemException("No java compiler in the current Java. This must be run on a JDK rather on a JRE");
			}

			JavaFileManager fm = new TestClassFileManager<JavaFileManager>(jc.getStandardFileManager(null, null, null), targetDir, getClass().getClassLoader());

			try
			{
				assertTrue(jc.getTask(null, fm, null, Collections.singletonList("-Xlint:deprecation"), null, Collections.singletonList(new JavaSourceFromString("At4JDocTest", s))).call().booleanValue());
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}

			ClassLoader cl = new EntityClassLoader(targetDir.getFileSystem().getLogAdapterHolder(), Thread.currentThread().getContextClassLoader(), Collections.singletonList(targetDir));
			try
			{
				Class<?> c = cl.loadClass("At4JDocTest");
				Object o = c.newInstance();
				return c.getMethod("runTest", new Class[] { Object[].class }).invoke(o, new Object[] { args });
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		finally
		{
			new IteratorDeleter(targetDir).delete();
			targetDir.getFileSystem().close();
			assertTrue(targetD.delete());
		}
	}

	/**
	 * @param testFile The test file.
	 * @param testMethod A method named runTest that runs the tests.
	 */
	protected Object runExampleClassTest(String testFile, String testMethod, Object[] args, List<TwoObjects<String, String>> replaces) throws IOException
	{
		return runExampleTest(CLASS_TEST_CODE_PREFIX + runReplaces(Files.readTextFile(getDocumentationFile(testFile)), replaces) + testMethod + CLASS_TEST_CODE_SUFFIX, args);
	}

	protected Object runExampleMethodTest(String testFile, String codePrefix, String codeSuffix, String otherCode, Object[] args, List<TwoObjects<String, String>> replaces) throws IOException
	{
		return runExampleTest(METHOD_TEST_CODE_PREFIX + (otherCode != null ? otherCode : "") + METHOD_TEST_METHOD_PREFIX + (codePrefix != null ? codePrefix : "")
				+ runReplaces(Files.readTextFile(getDocumentationFile(testFile), Charsets.UTF8), replaces) + (codeSuffix != null ? codeSuffix : "return null;") + METHOD_TEST_CODE_SUFFIX, args);
	}

	@Test
	public void testEscapeBackslashes()
	{
		assertEquals("\\\\\\\\", escapeBackslashes("\\"));
	}
}
