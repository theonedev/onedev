package io.onedev.server.util;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import io.onedev.k8shelper.KubernetesHelper;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private static final PrettyTime PRETTY_TIME = new PrettyTime();
	
	public static final List<String> RELAX_DATE_EXAMPLES = Lists.newArrayList(
			"2:30pm", "4-23", "2018-2-3", "one hour ago", "2 hours ago", "3PM", "noon", "today", 
			"yesterday", "yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "last month", "1 month 2 days ago", "last year", "1 year ago"); 
	
    public static String formatAge(Date date) {
    	return PRETTY_TIME.format(date);
    }

    public static String formatDate(Date date) {
        return Constants.DATE_FORMATTER.print(new DateTime(date));
    }

    public static String formatDateTime(Date date) {
        return Constants.DATETIME_FORMATTER.print(new DateTime(date));
    }

    @Nullable
    public static Date parseRelaxed(String relaxed) {
    	Parser parser = new Parser();
    	List<DateGroup> groups = parser.parse(relaxed);
    	if (!groups.isEmpty() && !groups.get(0).getDates().isEmpty())
    		return groups.get(0).getDates().get(0);
    	else
    		return null;
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
	
	public static LocalDate toLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static Date toDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());		
	}
	
}