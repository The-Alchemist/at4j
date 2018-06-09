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
package org.at4j.zip.ef;

import java.util.HashMap;
import java.util.Map;

import org.at4j.support.lang.UnsignedShort;

/**
 * This is a registry for {@link ZipEntryExtraFieldParser} objects. The objects
 * are accessed by the codes that identify them in the Zip file.
 * <p>
 * The registry is used by the {@link org.at4j.zip.ZipFile} object when parsing
 * the Zip file.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipEntryExtraFieldParserRegistry
{
	private static final Map<UnsignedShort, ZipEntryExtraFieldParser> m_registry = new HashMap<UnsignedShort, ZipEntryExtraFieldParser>();

	/**
	 * Create the registry. By default the registry contains all factories from
	 * this package.
	 */
	public ZipEntryExtraFieldParserRegistry()
	{
		m_registry.put(ExtendedTimestampExtraField.CODE, ExtendedTimestampExtraFieldParser.INSTANCE);
		m_registry.put(InfoZipUnixExtraField.CODE, InfoZipUnixExtraFieldParser.INSTANCE);
		m_registry.put(NewInfoZipUnixExtraField.CODE, NewInfoZipUnixExtraFieldParser.INSTANCE);
		m_registry.put(NtfsExtraField.CODE, NtfsExtraFieldParser.INSTANCE);
		m_registry.put(UnicodePathExtraField.CODE, UnicodePathExtraFieldParser.INSTANCE);
		m_registry.put(UnicodeCommentExtraField.CODE, UnicodeCommentExtraFieldParser.INSTANCE);
	}

	/**
	 * Register a new extra field parser.
	 * <p>
	 * If another parser is already registered for the same extra field code,
	 * that parser is unregistered.
	 * @param p The parser to register.
	 */
	public void registerParser(ZipEntryExtraFieldParser p)
	{
		m_registry.put(p.getCode(), p);
	}

	/**
	 * Get the extra field parser registered under the supplied code. If no
	 * parser is registered for that code, an
	 * {@link UnparsedZipEntryExtraFieldParser} is returned.
	 * @param code The code identifying the extra field in the Zip file.
	 * @return An extra field parser.
	 */
	public ZipEntryExtraFieldParser forCode(UnsignedShort code)
	{
		ZipEntryExtraFieldParser res = m_registry.get(code);
		return res != null ? res : UnparsedZipEntryExtraFieldParser.INSTANCE;
	}
}
