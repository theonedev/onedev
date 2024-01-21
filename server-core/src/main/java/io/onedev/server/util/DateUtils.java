package io.onedev.server.util;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import io.onedev.agent.ExecutorUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import javax.annotation.Nullable;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private static final PrettyTime PRETTY_TIME = new PrettyTime();
	
	public static final List<String> RELAX_DATE_EXAMPLES = Lists.newArrayList(
			"2:30pm", "4-23", "2018-2-3", "one hour ago", "2 hours ago", "3PM", "noon", "today", 
			"yesterday", "yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "last month", "1 month 2 days ago", "last year", "1 year ago"); 

	private static final Pattern WORKING_PERIOD_PATTERN = Pattern.compile("(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?");
	
	public static final String WORKING_PERIOD_HELP = "Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. " +
			"For instance <tt>1w 1d 1h 1m</tt> represents 1 week (5 working days), 1 day (8 working hours), 1 hour, and 1 minute";
	
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
		return ExecutorUtils.formatDuration(durationMillis);
    }
    
	public static Date parseISO8601Date(String dateString) {
		return ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).toDate();
	}
	
	public static String formatISO8601Date(Date date) {
		return ISODateTimeFormat.dateTime().print(date.getTime());
	}
	
	public static int parseWorkingPeriod(String period) {
		period = StringUtils.deleteWhitespace(period);
		if (StringUtils.isBlank(period))
			throw new ValidationException("Invalid working period");
		
		if (period.equals("0"))
			return 0;
		
		Matcher matcher = WORKING_PERIOD_PATTERN.matcher(period);
		if (!matcher.matches()) 
			throw new ValidationException("Invalid working period");
		
		int minutes = 0;
		if (matcher.group(1) != null) {
			int weeks = Integer.parseInt(StringUtils.stripEnd(matcher.group(1), "w"));
			minutes += weeks*5*8*60;
		}
		
		if (matcher.group(2) != null) {
			int days = Integer.parseInt(StringUtils.stripEnd(matcher.group(2), "d"));
			minutes += days*8*60;
		}
		
		if (matcher.group(3) != null) {
			int hours = Integer.parseInt(StringUtils.stripEnd(matcher.group(3), "h"));
			minutes += hours*60;
		}
		
		if (matcher.group(4) != null) 
			minutes += Integer.parseInt(StringUtils.stripEnd(matcher.group(4), "m"));
		
		return minutes;
	}
	
	public static String formatWorkingPeriod(int minutes) {
		int weeks = minutes/(60*8*5);
		minutes = minutes%(60*8*5);
		int days = minutes/(60*8);
		minutes = minutes%(60*8);
		int hours = minutes/60;
		minutes = minutes%60;
		
		StringBuilder builder = new StringBuilder();
		if (weeks != 0)
			builder.append(weeks).append("w ");
		if (days != 0)
			builder.append(days).append("d ");
		if (hours != 0)
			builder.append(hours).append("h ");
		if (minutes != 0)
			builder.append(minutes).append("m");
		
		String formatted = builder.toString().trim();
		if (formatted.length() == 0)
			formatted = "0m";
		return formatted;
	}

	public static LocalDate toLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static Date toDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());		
	}
}