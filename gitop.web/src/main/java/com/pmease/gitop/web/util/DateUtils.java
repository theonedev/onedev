package com.pmease.gitop.web.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Preconditions;
import com.pmease.gitop.web.Constants;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static final String AGE = "age";

    static enum DurationUnit {
        YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND
    }

    static String formatAge(DateTime dtBefore, DateTime dtNow) {
        return formatAge(dtBefore, dtNow, Constants.DATETIME_FORMAT);
    }

    static String formatAge(DateTime dtBefore, DateTime dtNow,
            String fullDateFormat) {
        Preconditions.checkArgument(dtBefore != null && dtNow != null);

        Period period = new Period(dtBefore, dtNow);

        int years = period.getYears();
        int months = period.getMonths();
        int weeks = period.getWeeks();
        int days = Days.daysBetween(dtBefore.toDateMidnight(),
                dtNow.toDateMidnight()).getDays();

//        if (years > 0 || months > 0 || weeks > 0 || days > 6) {
//            return DateUtils.formatDate(dtBefore.toDate(), fullDateFormat);
//        }

        if (years > 0) {
        	return formatDurationPart(years, DurationUnit.YEAR);
        }
        
        if (months > 0) {
        	return formatDurationPart(months, DurationUnit.MONTH);
        }
        
        if (weeks > 0) {
        	return formatDurationPart(weeks, DurationUnit.WEEK);
        }
        
        if (days > 1) {
            return formatDurationPart(days, DurationUnit.DAY);
        }

        // acutal hours
        int hours = Hours.hoursBetween(
                dtBefore.hourOfDay().roundHalfFloorCopy(),
                dtNow.hourOfDay().roundHalfFloorCopy()).getHours();

        if ((hours >= DateTimeConstants.HOURS_PER_DAY)
                || (days == 1 && hours > 12)) {
            return formatDurationPart(days, DurationUnit.DAY);
        }

        if (hours > 0) {
            return formatDurationPart(hours, DurationUnit.HOUR);
        }

        int minutes = period.getMinutes();
        if (minutes > 0) {
            return formatDurationPart(minutes, DurationUnit.MINUTE);
        }
        return formatDurationPart(period.getSeconds(), DurationUnit.SECOND);
    }

    private static String formatDurationPart(int dur, DurationUnit unit) {
        if (unit == DurationUnit.SECOND && dur < 30) { // < 30 seconds
            return "just now";
        }

        StringBuffer sb = new StringBuffer();
        
        if (dur == 1) {
        	if (unit == DurationUnit.HOUR) {
        		sb.append("an");
        	} else {
        		sb.append("a");
        	}
        } else {
        	sb.append(dur);
        }
        
        sb.append(" ").append(unit.name().toLowerCase());
        
        if (dur > 1) {
            sb.append("s");
        }
        sb.append(" ago");

        return sb.toString();
    }

    public static String formatAge(Date date) {
        return formatAge(new DateTime(date), DateTime.now());
    }

    public static String formatDate(Date date) {
        return formatDate(date, Constants.DATE_FORMAT);
    }

    public static String formatDateTime(Date date) {
        return formatDate(date, Constants.DATETIME_FORMAT);
    }

    public static String formatDate(Date date, String pattern) {
        return DateTimeFormat.forPattern(pattern)
                .withZone(DateTimeZone.UTC)
                .print(new DateTime(date));
    }

    public static String formatDuration(long durationMillis) {
        if (durationMillis < 1000) {
            return durationMillis + " ms";
        } else {
            return DurationFormatUtils.formatDurationWords(durationMillis,
                    true, true);
        }
    }

    public static String formatDurationShortWords(long durationMills) {
        if (durationMills < DateTimeConstants.MILLIS_PER_SECOND) {
            return durationMills + " ms";
        } else {
            Duration duration = new Duration(durationMills);
            return shortWordFormatter.print(duration.toPeriod(
                    PeriodType.yearMonthDayTime(),
                    ISOChronology.getInstanceUTC()));
        }
    }

    public static boolean isSameDay(Date d1, Date d2) {
    	Calendar cal1 = Calendar.getInstance(DateTimeZone.UTC.toTimeZone());
    	Calendar cal2 = Calendar.getInstance(DateTimeZone.UTC.toTimeZone());
    	cal1.setTime(d1);
    	cal2.setTime(d2);
    	
    	return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    			&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    			&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    public static void main(String[] argv) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2012);
        cal.set(Calendar.MONTH, 9); // Oct.
        cal.set(Calendar.DAY_OF_MONTH, 21);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.setTimeZone(DateTimeZone.forID("America/Sao_Paulo").toTimeZone());
        DateTime dt = new DateTime(cal);
        try {
            dt.withMillisOfDay(0);
        } catch (IllegalArgumentException e) {
            // Illegal instant due to time zone offset transition
            e.printStackTrace();
        }
        
        System.out.println(dt.toDateMidnight());
    }
    
    static PeriodFormatter shortWordFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d").appendSeparator(", ").appendHours()
            .appendSuffix("h").appendSeparator(":").appendMinutes()
            .appendSuffix("m").appendSeparator(":").appendSeconds()
            .appendSuffix("s")
            // .appendSeparator(", ")
            // .appendMillis()
            .toFormatter();
}