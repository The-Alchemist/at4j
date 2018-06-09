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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import org.entityfs.Directory;
import org.entityfs.util.cap.entity.ECFileResolvableUtil;

/**
 * This class was copy-pasted from the EntityFS documentation tests.
 */
class TestClassFileManager<T extends JavaFileManager> extends ForwardingJavaFileManager<T>
{
	/**
	 * Extend the {@code SimpleJavaFileObject} to be able to use its constructor.
	 * @author Karl Gustafsson
	 */
	private static final class FooSimpleJavaFileObject extends SimpleJavaFileObject
	{
		public FooSimpleJavaFileObject(URI loc, JavaFileObject.Kind kind)
		{
			super(loc, kind);
		}

		@Override
		public OutputStream openOutputStream() throws IOException
		{
			return new FileOutputStream(new File(uri.getPath()));
		}
	}
	
	private final Directory m_outDir;
	private final ClassLoader m_classLoader;

	TestClassFileManager(T mgr, Directory outDir, ClassLoader cl)
	{
		super(mgr);
		m_outDir = outDir;
		m_classLoader = cl;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location loc, String className, JavaFileObject.Kind kind, FileObject sibling)
	{
		return new FooSimpleJavaFileObject(new File(ECFileResolvableUtil.getFileObject(m_outDir), className + ".class").toURI(), kind);
	}

	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location loc)
	{
		return m_classLoader;
	}
}
