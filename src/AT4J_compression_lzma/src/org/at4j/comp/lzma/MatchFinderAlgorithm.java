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
package org.at4j.comp.lzma;

/**
 * This is an enumeration over the different match finder algorithms that are
 * known by the LZMA encoder.
 * @author Karl Gustafsson
 * @since 1.0
 */
public enum MatchFinderAlgorithm
{
	BINARY_TREE_2(0, "bt2"), BINARY_TREE_4(1, "bt4");

	private final int m_id;
	private final String m_tag;

	private MatchFinderAlgorithm(int id, String tag)
	{
		m_id = id;
		m_tag = tag;
	}

	/**
	 * Get the id that this match finder algorithm is identified by when used
	 * with the LZMA encoder.
	 * @return This match finder algorithm's id.
	 */
	public int getId()
	{
		return m_id;
	}

	@Override
	public String toString()
	{
		return m_tag;
	}
}
