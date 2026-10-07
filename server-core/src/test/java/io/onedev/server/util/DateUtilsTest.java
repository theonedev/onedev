package io.onedev.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {

	@Test
	public void testRelaxedDateExamples() {
		for (var zone : List.of("UTC", "Asia/Shanghai", "America/New_York")) {
			for (var example : DateUtils.RELAX_DATE_EXAMPLES)
				assertNotNull(DateUtils.parseRelaxed(example, ZoneId.of(zone)), example + " in " + zone);
		}
	}

	@Test
	public void testCalendarBasedDates() {
		// Holidays and seasons exercise Natty's binary compatibility with iCal4j.
		// Its bundled season calendar only covers dates through 2020.
		for (var zone : List.of("UTC", "Asia/Shanghai", "America/New_York")) {
			assertDate("Christmas 2018", "2018-12-25", zone);
			assertDate("Thanksgiving 2018", "2018-11-22", zone);
			assertDate("winter 2018", "2018-12-21", zone);
			assertDate("summer 2018", "2018-06-21", zone);
		}
	}

	@Test
	public void testUnrecognizedDate() {
		assertNull(DateUtils.parseRelaxed("not a date", ZoneId.of("UTC")));
	}

	private void assertDate(String input, String expected, String zone) {
		var zoneId = ZoneId.of(zone);
		var date = DateUtils.parseRelaxed(input, zoneId);
		assertNotNull(date, input + " in " + zone);
		assertEquals(LocalDate.parse(expected), date.toInstant().atZone(zoneId).toLocalDate(),
				input + " in " + zone);
	}

}
