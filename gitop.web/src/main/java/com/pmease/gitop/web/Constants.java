package com.pmease.gitop.web;

import java.util.Date;

public class Constants {
	private Constants() {
	}

	public static final Date NULL_DATE = new Date(0);
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_FULL_FORMAT = "EEEEE, MMM dd, yyyy, HH:mm:ss Z";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String TIMEZONE_FORMAT = "Z";

	public static final String INPUT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String INPUT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String INPUT_TIME_FORMAT = "HH:mm";

	public static final String INPUT_DATE_FORMAT_WITH_ZONE = "yyyy-MM-dd Z";
	public static final String INPUT_DATETIME_FORMAT_WITH_ZONE = "yyyy-MM-dd HH:mm Z";
	public static final String INPUT_TIME_FORMAT_WITH_ZONE = "HH:mm Z";

	public static final String SYSTEM_LOG_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

	public static final String UTF8 = "UTF-8";

	public static final String COOKIE_CREDENTIAL = "gitop.login";

}
