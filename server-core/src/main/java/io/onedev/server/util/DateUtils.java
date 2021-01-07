package io.onedev.server.util;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

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
    	if (durationMillis < 0)
    		durationMillis = 0;
    	return DurationFormatUtils.formatDurationWords(durationMillis, true, true);
    }
}