package org.at4j.support.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.at4j.support.lang.UnsignedShort;
import org.junit.Test;

public class MsDosTimeTest
{
	@Test
	public void testParse()
	{
		short time = 23;
		time += 24 << 5;
		time += 8 << 11;
		Calendar cal = Calendar.getInstance();
		cal.clear();
		MsDosTime.parseMsDosTime(UnsignedShort.valueOf(time), cal);

		assertEquals(46, cal.get(Calendar.SECOND));
		assertEquals(24, cal.get(Calendar.MINUTE));
		assertEquals(8, cal.get(Calendar.HOUR));

		assertEquals(time, MsDosTime.encodeMsDosTime(cal).intValue());

		// Set minute = 61
		time += 37 << 5;
		try
		{
			MsDosTime.parseMsDosTime(UnsignedShort.valueOf(time), cal);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(e.getMessage().contains("minute"));
		}

		// Set hour = 24
		time -= 37 << 5;
		time += 16 << 11;
		try
		{
			MsDosTime.parseMsDosTime(UnsignedShort.valueOf(time), cal);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(e.getMessage().contains("hour"));
		}
	}
}
