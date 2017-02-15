package com.gitplex.server.web.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import com.gitplex.server.web.WebConstants;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static String formatAge(Date date) {
    	return new PrettyTime().format(date);
    }

    public static String formatDate(Date date) {
        return WebConstants.DATE_FORMATTER.print(new DateTime(date));
    }

    public static String formatDateTime(Date date) {
        return WebConstants.DATETIME_FORMATTER.print(new DateTime(date));
    }

}