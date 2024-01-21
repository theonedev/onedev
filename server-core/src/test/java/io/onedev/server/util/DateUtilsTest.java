package io.onedev.server.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DateUtilsTest {

	@Test
	public void test() {
		assertEquals("4w 3d 6h", DateUtils.formatWorkingPeriod(DateUtils.parseWorkingPeriod("4w 0d 30h")));
		assertEquals("4w 3d 6h", DateUtils.formatWorkingPeriod(DateUtils.parseWorkingPeriod("4w 0d30h")));
		assertEquals("1w 3d 5h", DateUtils.formatWorkingPeriod(DateUtils.parseWorkingPeriod("8d 5h")));
		assertEquals("0m", DateUtils.formatWorkingPeriod(DateUtils.parseWorkingPeriod("0h")));
		assertEquals("0m", DateUtils.formatWorkingPeriod(DateUtils.parseWorkingPeriod("0m")));
		assertEquals("0m", DateUtils.formatWorkingPeriod(DateUtils.parseWorkingPeriod("0")));
		try {
			DateUtils.parseWorkingPeriod("abc");
			assertTrue(false);
		} catch (Exception e) {
		}
	}

}
