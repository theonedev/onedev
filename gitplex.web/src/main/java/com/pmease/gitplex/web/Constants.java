package com.pmease.gitplex.web;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constants {

	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern(DATETIME_FORMAT);
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT);

	public static final int DEFAULT_PAGE_SIZE = 25;
	
	public static final int DEFAULT_SELECT2_PAGE_SIZE = 25;

	/* diff constants */
	public static final int DIFF_EXPAND_SIZE = 15;
	public static final int MAX_DIFF_FILES = 500;
	public static final int MAX_SINGLE_FILE_DIFF_LINES = 5000;
	public static final int MAX_DIFF_LINES = 20000;
	
	/* commit constants */
	public static final int MAX_DISPLAY_COMMITS = 500;
	
}
