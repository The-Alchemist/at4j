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
 * This class contains static utility methods for working with MS DOS times. An
 * MS DOS time is a time in a day with two second precision. It can be encoded
 * in two bytes.
 * <p>
 * See <a href="http://support.microsoft.com/kb/38492">the Microsoft
 * documentation</a>.
 * @author Karl Gustafsson
 * @since 1.0
 * @see MsDosDate
 */
public final class MsDosTime
{
	/** Hidden constructor. */
	private MsDosTime()
	{
		// Nothing
	}

	/**
	 * Parse the MS DOS time (hour of day, minute and second with two-second
	 * precision) encoded in the supplied unsigned short value and update the
	 * calendar object.
	 * <p>
	 * The time should be encoded as described <a
	 * href="http://support.microsoft.com/kb/38492">here</a>.
	 * @param s The encoded time.
	 * @param cal The calendar to update with the time.
	 * @throws IllegalArgumentException If the encoded time is invalid.
	 */
	public static void parseMsDosTime(UnsignedShort s, Calendar cal) throws IllegalArgumentException
	{
		int intval = s.intValue();
		int second = 2 * (intval & 31);
		int minute = ((intval >> 5) & 63);
		int hour = (intval >> 11);

		if (minute > 59)
		{
			throw new IllegalArgumentException("Invalid MS-DOS time value " + s + ". The minute was " + minute);
		}
		if (hour > 23)
		{
			throw new IllegalArgumentException("Invalid MS-DOS time value " + s + ". The hour was " + hour);
		}
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.HOUR_OF_DAY, hour);
	}

	/**
	 * Encode the MS DOS time (hour of day, minute and second with two-second
	 * precision) stored in the supplied {@code Calendar} object to an unsigned
	 * short value.
	 * <p>
	 * The time is encoded as described <a
	 * href="http://support.microsoft.com/kb/38492">here</a>.
	 * @param cal The calendar object containing the time to encode.
	 * @return The encoded time.
	 */
	public static UnsignedShort encodeMsDosTime(Calendar cal)
	{
		int res = cal.get(Calendar.SECOND) / 2;
		res += (cal.get(Calendar.MINUTE) << 5);
		res += (cal.get(Calendar.HOUR_OF_DAY) << 11);
		return UnsignedShort.valueOf(res);
	}
}
