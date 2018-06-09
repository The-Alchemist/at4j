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

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * This class was copy-pasted from the EntityFS documentation tests. From
 * {@code JavaCompiler} API docs.
 */
public class JavaSourceFromString extends SimpleJavaFileObject
{
	/**
	 * The source code of this "file".
	 */
	final String code;

	/**
	 * Constructs a new JavaSourceFromString.
	 * @param name the name of the compilation unit represented by this file
	 * object
	 * @param code the source code for the compilation unit represented by this
	 * file object
	 */
	JavaSourceFromString(String name, String code)
	{
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
	{
		return code;
	}
}