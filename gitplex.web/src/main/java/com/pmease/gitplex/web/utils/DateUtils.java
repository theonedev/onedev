package com.pmease.gitplex.web.utils;

import java.util.Date;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import com.pmease.gitplex.web.Constants;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static String formatAge(Date date) {
    	return new PrettyTime().format(date);
    }

    public static String formatDate(Date date) {
        return Constants.DATE_FORMATTER.print(new DateTime(date));
    }

    public static String formatDateTime(Date date) {
        return Constants.DATETIME_FORMATTER.print(new DateTime(date));
    }

}