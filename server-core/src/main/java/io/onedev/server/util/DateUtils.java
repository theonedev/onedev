package io.onedev.server.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.web.WebSession;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private static final PrettyTime PRETTY_TIME = new PrettyTime();
	
	public static final List<String> RELAX_DATE_EXAMPLES = Lists.newArrayList(
			"2:30pm", "4-23", "2018-2-3", "one hour ago", "2 hours ago", "3PM", "noon", "today", 
			"yesterday", "yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "last month", "1 month 2 days ago", "last year", "1 year ago"); 
	
    public static String formatAge(Date date) {
    	return PRETTY_TIME.format(date);
    }

	public static ZoneId getZoneId() {
		if (RequestCycle.get() != null) {
			ZoneId zoneId = WebSession.get().getZoneId();
			if (zoneId == null)
				zoneId = ZoneId.systemDefault();
			return zoneId;
		} else {
			return ZoneId.systemDefault();
		}
	}

    public static String formatDate(Date date, ZoneId zoneId) {
        return Constants.DATE_FORMATTER.withZone(DateTimeZone.forID(zoneId.getId())).print(new DateTime(date));
    }

    public static String formatDate(Date date) {
        return formatDate(date, getZoneId());
    }

    public static String formatDateTime(Date date, ZoneId zoneId) {
        return Constants.DATETIME_FORMATTER.withZone(DateTimeZone.forID(zoneId.getId())).print(new DateTime(date));
    }

    public static String formatDateTime(Date date) {
        return formatDateTime(date, getZoneId());
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
		return KubernetesHelper.formatDuration(durationMillis);
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
