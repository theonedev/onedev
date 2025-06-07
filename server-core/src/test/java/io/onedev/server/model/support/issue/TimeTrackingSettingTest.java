package io.onedev.server.model.support.issue;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeTrackingSettingTest {
	
	@Test
	public void test() {
		var timeTrackingSetting = new TimeTrackingSetting();
		assertEquals("4w 3d 6h", timeTrackingSetting.formatWorkingPeriod(timeTrackingSetting.parseWorkingPeriod("4w 0d 30h"), false));
		assertEquals("4w 3d 6h", timeTrackingSetting.formatWorkingPeriod(timeTrackingSetting.parseWorkingPeriod("4w 0d30h"), false));
		assertEquals("1w 3d 5h", timeTrackingSetting.formatWorkingPeriod(timeTrackingSetting.parseWorkingPeriod("8d 5h"), false));
		assertEquals("0h", timeTrackingSetting.formatWorkingPeriod(timeTrackingSetting.parseWorkingPeriod("0h"), false));
		assertEquals("0h", timeTrackingSetting.formatWorkingPeriod(timeTrackingSetting.parseWorkingPeriod("0m"), false));
		assertEquals("0h", timeTrackingSetting.formatWorkingPeriod(timeTrackingSetting.parseWorkingPeriod("0"), false));
		try {
			timeTrackingSetting.parseWorkingPeriod("abc");
			assertTrue(false);
		} catch (Exception e) {
		}
	}
	
}