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
package org.at4j.zip.extattrs;

import java.util.HashMap;
import java.util.Map;

import org.at4j.zip.ZipVersionMadeBy;

/**
 * This is a registry where parsers for the different
 * {@link ZipExternalFileAttributes} that may occur in a Zip file are
 * registered. A parser is identified by the version of the Zip software that
 * was used to create the Zip file (the {@link ZipVersionMadeBy}).
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipExternalFileAttributesParserRegistry
{
	private final Map<ZipVersionMadeBy, ZipExternalFileAttributesParser> m_registry = new HashMap<ZipVersionMadeBy, ZipExternalFileAttributesParser>();

	/**
	 * Create a new registry.
	 * <p>
	 * The new registry contains all file attribute compatibility classes
	 * defined in this package.
	 */
	public ZipExternalFileAttributesParserRegistry()
	{
		m_registry.put(ZipVersionMadeBy.MSDOS, MsDosExternalFileAttributesParser.INSTANCE);
		m_registry.put(ZipVersionMadeBy.UNIX, UnixExternalFileAttributesParser.INSTANCE);
		m_registry.put(ZipVersionMadeBy.WINDOWS_NTFS, NtfsExternalFileAttributesParser.INSTANCE);
	}

	/**
	 * Register a new external file attributes parser object.
	 * @param p The parser to register.
	 */
	public void registerExternalFileAttributesParser(ZipExternalFileAttributesParser p)
	{
		m_registry.put(p.getVersionMadeBy(), p);
	}

	/**
	 * Get the parser for the specific Zip software version.
	 * @param vmb The Zip software version.
	 * @return The parser for the Zip software version, or an
	 * {@link UnixExternalFileAttributesParser} if the software version is not
	 * known.
	 */
	public ZipExternalFileAttributesParser forVersionMadeBy(ZipVersionMadeBy vmb)
	{
		ZipExternalFileAttributesParser res = m_registry.get(vmb);
		return res != null ? res : UnparsedExternalFileAttributesParser.INSTANCE;
	}
}
