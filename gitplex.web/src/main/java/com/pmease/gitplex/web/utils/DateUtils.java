package com.pmease.gitplex.web.utils;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import com.pmease.gitplex.web.Constants;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static String formatAge(Date date) {
    	return new PrettyTime().format(date);
    }

    public static String formatDate(Date date) {
        return formatDate(date, Constants.DATE_FORMAT);
    }

    public static String formatDateTime(Date date) {
        return formatDate(date, Constants.DATETIME_FORMAT);
    }

    public static String formatDate(Date date, String pattern) {
        return DateTimeFormat.forPattern(pattern).print(new DateTime(date));
    }

}