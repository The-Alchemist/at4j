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
package org.at4j.support.util;

import java.util.Calendar;

import org.at4j.support.lang.UnsignedShort;

/**
 * This class contains static utility methods for working with MS DOS dates. An
 * MS DOS date is a year, a month and a day of month and it can be encoded in
 * two bytes.
 * <p>
 * See <a href="http://support.microsoft.com/kb/38492">the Microsoft
 * documentation</a>.
 * @author Karl Gustafsson
 * @since 1.0
 * @see MsDosTime
 */
public final class MsDosDate
{
	/**
	 * Hidden constructor
	 */
	private MsDosDate()
	{
		// Nothing
	}

	/**
	 * Parse the MS DOS date (year, month and day of month) encoded in the
	 * supplied unsigned short value and update the calendar object.
	 * <p>
	 * The date should be encoded as described <a
	 * href="http://support.microsoft.com/kb/38492">here</a>.
	 * @param s The encoded date.
	 * @param cal The calendar to update with the date.
	 * @throws IllegalArgumentException If the encoded date is invalid.
	 */
	public static void parseMsDosDate(UnsignedShort s, Calendar cal) throws IllegalArgumentException
	{
		int intval = s.intValue();
		int dayOfMonth = (intval & 31);
		int monthOfYear = ((intval >> 5) & 15);
		int year = 1980 + (intval >> 9);

		if (dayOfMonth == 0)
		{
			throw new IllegalArgumentException("Invalid MS-DOS date value " + s + ". The day of month was " + dayOfMonth);
		}
		if (monthOfYear == 0 || monthOfYear > 12)
		{
			throw new IllegalArgumentException("Invalid MS-DOS date value " + s + ". The month of year was " + monthOfYear);
		}
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		cal.set(Calendar.MONTH, monthOfYear - 1);
		cal.set(Calendar.YEAR, year);
	}

	/**
	 * Encode the MS DOS date (year, month and day of month) stored in the
	 * {@code Calendar} object to an unsigned short value.
	 * <p>
	 * The date is encoded as described <a
	 * href="http://support.microsoft.com/kb/38492">here</a>.
	 * @param cal The calendar object containing the date to encode.
	 * @return The encoded date.
	 */
	public static UnsignedShort encodeMsDosDate(Calendar cal)
	{
		int res = cal.get(Calendar.DAY_OF_MONTH);
		res += ((cal.get(Calendar.MONTH) + 1) << 5);
		res += ((cal.get(Calendar.YEAR) - 1980) << 9);
		return UnsignedShort.valueOf(res);
	}
}
