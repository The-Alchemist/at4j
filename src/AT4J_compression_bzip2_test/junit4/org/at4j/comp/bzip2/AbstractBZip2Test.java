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
package org.at4j.comp.bzip2;

import java.util.Random;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
abstract class AbstractBZip2Test
{
	// Works only for ASCII strings...
	final byte[] createData(String s)
	{
		byte[] res = new byte[s.length()];
		char[] carr = s.toCharArray();
		for (int i = 0; i < carr.length; i++)
		{
			res[i] = (byte) (carr[i] & 0xFF);
		}
		return res;
	}

	final byte[] createThreeCharAlphabetData(int len, Random r)
	{
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++)
		{
			int n = r.nextInt(3);
			switch (n)
			{
				case 0:
					res[i] = (byte) 'A';
					break;
				case 1:
					res[i] = (byte) 'B';
					break;
				default:
					res[i] = (byte) 'C';
			}
		}
		return res;
	}

	final byte[] createData(int len, Random r)
	{
		byte[] res = new byte[len];
		r.nextBytes(res);
		return res;
	}

	final String createDataString(byte[] data)
	{
		char[] carr = new char[data.length];
		for (int i = 0; i < data.length; i++)
		{
			carr[i] = (char) data[i];
		}
		return new String(carr);
	}
}
