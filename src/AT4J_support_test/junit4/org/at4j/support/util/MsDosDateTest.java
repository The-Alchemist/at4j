package org.at4j.support.util;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.at4j.support.lang.UnsignedShort;
import org.junit.Test;

public class MsDosDateTest
{
	@Test
	public void testParse()
	{
		short time = 5;
		time += 4 << 5;
		time += 29 << 9;
		Calendar cal = Calendar.getInstance();
		cal.clear();
		MsDosDate.parseMsDosDate(UnsignedShort.valueOf(time), cal);

		assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));
		// January == 0
		assertEquals(3, cal.get(Calendar.MONTH));
		assertEquals(2009, cal.get(Calendar.YEAR));

		assertEquals(time, MsDosDate.encodeMsDosDate(cal).intValue());

		// Set day = zero
		time -= 5;
		try
		{
			MsDosDate.parseMsDosDate(UnsignedShort.valueOf(time), cal);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(e.getMessage().contains("day"));
		}

		// Set month = 13
		time += 5 + 9 << 5;
		try
		{
			MsDosDate.parseMsDosDate(UnsignedShort.valueOf(time), cal);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(e.getMessage().contains("month"));
		}
	}
}
