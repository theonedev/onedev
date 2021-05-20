package io.onedev.server.rest;

import java.util.Date;

import org.joda.time.format.ISODateTimeFormat;

public class RestUtils {

	public static final int MAX_PAGE_SIZE = 100;

	public static Date toDate(String dateString) {
		return ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).toDate();
	}
	
}
