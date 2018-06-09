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

/**
 * An enumeration over the different Unix entity types that may occur in a Zip
 * file. This is used by the {@link UnixExternalFileAttributes}.
 * @author Karl Gustafsson
 * @since 1.0
 * @see UnixExternalFileAttributes
 */
public enum UnixEntityType
{
	REGULAR_FILE('-', 010), DIRECTORY('d', 04), SYMBOLIC_LINK('l', 012), BLOCK_SPECIAL('b', 06), CHARACTER_SPECIAL('c', 02), PIPE('p', 01), SOCKET('s', 014);

	private final char m_character;
	private final byte m_code;

	private UnixEntityType(char c, int code)
	{
		m_character = c;
		m_code = (byte) code;
	}

	/**
	 * Get the Unix entity type corresponding to the code.
	 * <p>
	 * The entity types are (codes in octal representation):
	 * <ul>
	 * <li>01 - PIPE</li>
	 * <li>02 - CHARACTER_SPECIAL</li>
	 * <li>04 - DIRECTORY</li>
	 * <li>06 - BLOCK_SPECIAL</li>
	 * <li>010 - REGULAR_FILE</li>
	 * <li>012 - SYMBOLIC_LINK</li>
	 * <li>014 - SOCKET</li>
	 * </ul>
	 * @param code The code for the entity type.
	 * @return The entity type.
	 * @throws IllegalArgumentException If the code is unknown.
	 */
	public static UnixEntityType forCode(int code) throws IllegalArgumentException
	{
		switch (code)
		{
			case 010:
				return REGULAR_FILE;
			case 04:
				return DIRECTORY;
			case 012:
				return SYMBOLIC_LINK;
			case 06:
				return BLOCK_SPECIAL;
			case 02:
				return CHARACTER_SPECIAL;
			case 01:
				return PIPE;
			case 014:
				return SOCKET;
			default:
				throw new IllegalArgumentException("Unknown entity type code " + code);
		}
	}

	/**
	 * Get the code for this entity type.
	 * @return The code for this entity type.
	 * @see #forCode(int)
	 */
	public byte getCode()
	{
		return m_code;
	}

	@Override
	public String toString()
	{
		return "" + m_character;
	}
}
