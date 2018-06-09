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

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.at4j.support.lang.UnsignedLong;

/**
 * This object represent Windows NT time. The time value is an unsigned 64-bit
 * value (an {@link UnsignedLong}) that counts all 100 nanosecond intervals that
 * have passed since midnight January 1, 1601 UTC.
 * <p>
 * Instances of this class are immutable.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class WinNtTime implements Serializable, Comparable<WinNtTime>
{
	private static final long serialVersionUID = 1L;

	private static final BigInteger JAVA_TIME_OFFSET = BigInteger.valueOf(11644473600000L);

	private final UnsignedLong m_time;

	/**
	 * Create a new {@code WinNtTime} from the numerical value.
	 * @param t The time value.
	 */
	public WinNtTime(UnsignedLong t)
	{
		// Null check
		t.getClass();
		m_time = t;
	}

	/**
	 * Create a new {@code WinNtTime} from the {@link Date} value.
	 * @param d The date.
	 */
	public WinNtTime(Date d)
	{
		// Null check
		d.getClass();
		m_time = UnsignedLong.valueOf(BigInteger.valueOf(d.getTime()).add(JAVA_TIME_OFFSET).multiply(BigInteger.valueOf(10000L)));
	}

	/**
	 * Get the time as an unsigned long.
	 * @return The time as an unsigned long.
	 */
	public UnsignedLong getTime()
	{
		return m_time;
	}

	/**
	 * Get this Windows NT time value as a {@link Date} value.
	 * @return A {@link Date} value.
	 * @throws UnableToConvertException If the Windows NT time value is outside
	 * the range of Java's {@link Date}.
	 */
	public Date getDate() throws UnableToConvertException
	{
		BigInteger d = m_time.bigIntValue().divide(BigInteger.valueOf(10000L)).subtract(JAVA_TIME_OFFSET);
		if ((d.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) || (d.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0))
		{
			throw new UnableToConvertException("Unable to convert the Windows NT time value " + this + " to a Java Date: the value is outside Date's range");
		}
		return new Date(d.longValue());
	}

	public int compareTo(WinNtTime t2)
	{
		return m_time.compareTo(t2.m_time);
	}

	@Override
	public boolean equals(Object o)
	{
		return ((o != null) && (o instanceof WinNtTime) && (((WinNtTime) o).m_time.equals(m_time)));
	}

	@Override
	public int hashCode()
	{
		return m_time.hashCode();
	}

	@Override
	public String toString()
	{
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(getDate());
	}
}
