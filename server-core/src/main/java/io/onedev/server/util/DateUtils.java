package io.onedev.server.util;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Application;
import org.joda.time.format.ISODateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.web.WebSession;

public class DateUtils {

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public static final String TIME_FORMAT = "HH:mm";

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
	
	public static final List<String> RELAX_DATE_EXAMPLES = Lists.newArrayList(
			"2:30pm", "4-23", "2018-2-3", "one hour ago", "2 hours ago", "3PM", "noon", "today", 
			"yesterday", "yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "last month", "1 month 2 days ago", "last year", "1 year ago"); 
	
    public static String formatAge(Date date) {
		if (Application.exists())
    		return new PrettyTime(WebSession.get().getLocale()).format(date);
		else
			return new PrettyTime().format(date);
    }

	public static ZoneId getZoneId() {
		if (Application.exists()) {
			ZoneId zoneId = WebSession.get().getZoneId();
			if (zoneId == null)
				zoneId = ZoneId.systemDefault();
			return zoneId;
		} else {
			return ZoneId.systemDefault();
		}
	}

	public static Date parseDate(String dateString, int hour, int minute, int second) {
		return parseDate(dateString, getZoneId(), hour, minute, second);
	}

	public static Date parseDateTime(String dateString) {
		return parseDateTime(dateString, getZoneId());
	}
	
	public static Date parseDate(String dateString, ZoneId zoneId, int hour, int minute, int second) {
		return Date.from(LocalDate.from(DATE_FORMATTER.parse(dateString)).atStartOfDay(zoneId).toInstant());
	}

	public static Date parseDateTime(String dateString, ZoneId zoneId) {
		return Date.from(LocalDateTime.from(DATETIME_FORMATTER.parse(dateString)).atZone(zoneId).toInstant());
	}

    public static String formatDate(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).format(DATE_FORMATTER);
    }

    public static String formatDate(Date date) {
        return formatDate(date, getZoneId());
    }

    public static String formatDateTime(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).format(DATETIME_FORMATTER);
    }

    public static String formatDateTime(Date date) {
        return formatDateTime(date, getZoneId());
    }

    public static String formatTime(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).format(TIME_FORMATTER);
    }

    public static String formatTime(Date date) {
        return formatTime(date, getZoneId());
    }

    @Nullable
    public static Date parseRelaxed(String relaxed, ZoneId zoneId) {
    	Parser parser = new Parser(TimeZone.getTimeZone(zoneId.getId()));
    	List<DateGroup> groups = parser.parse(relaxed);
    	if (!groups.isEmpty() && !groups.get(0).getDates().isEmpty())
    		return groups.get(0).getDates().get(0);
    	else
    		return null;
    }
	
    @Nullable
    public static Date parseRelaxed(String relaxed) {
    	return parseRelaxed(relaxed, getZoneId());
    }
	
    public static String formatDuration(long durationMillis) {
		var duration = KubernetesHelper.formatDuration(durationMillis);
		var number = StringUtils.substringBefore(duration, " ");
		var unit = StringUtils.substringAfter(duration, " ");		
		return MessageFormat.format(_T("{0} " + unit), number);
    }
    
	public static Date parseISO8601Date(String dateString) {
		return ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).toDate();
	}
	
	public static String formatISO8601Date(Date date) {
		return ISODateTimeFormat.dateTime().print(date.getTime());
	}
	
	public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
		return date.toInstant().atZone(zoneId).toLocalDate();
	}
	
	public static LocalDate toLocalDate(Date date) {
		return toLocalDate(date, getZoneId());
	}
	
	public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
		return Date.from(localDateTime.atZone(zoneId).toInstant());		
	}
	
	public static Date toDate(LocalDateTime localDateTime) {
		return toDate(localDateTime, getZoneId());
	}

}
