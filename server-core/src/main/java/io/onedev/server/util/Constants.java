package io.onedev.server.util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constants {

	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
	
	public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern(DATETIME_FORMAT);
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT);

}
