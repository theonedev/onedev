package io.onedev.server.model.support.issue;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ValidationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.util.usage.Usage;

@Editable
public class TimeTrackingSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Pattern WORKING_PERIOD_PATTERN = Pattern.compile("(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?");

	private static final Pattern HOURS_AND_MINUTES_ONLY_WORKING_PERIOD_PATTERN = Pattern.compile("(\\d+h)?(\\d+m)?");
	
	private boolean useHoursAndMinutesOnly;
	
	private int hoursPerDay = 8;

	private int daysPerWeek = 5;

	private String aggregationLink;

	@Editable(order=50, description = "Whether or not to input and display estimated/spent time " +
			"in hours/minutes only")
	public boolean isUseHoursAndMinutesOnly() {
		return useHoursAndMinutesOnly;
	}

	public void setUseHoursAndMinutesOnly(boolean useHoursAndMinutesOnly) {
		this.useHoursAndMinutesOnly = useHoursAndMinutesOnly;
	}

	@Editable(order=100, description = "Specify working hours per day. This will affect " +
			"parsing and displaying of working periods. For instance <tt>1d</tt> is the " +
			"same as <tt>8h</tt> if this property is set to <tt>8</tt>")
	@DependsOn(property="useHoursAndMinutesOnly", value="false")
	@Max(24)
	@Min(1)
	public int getHoursPerDay() {
		return hoursPerDay;
	}

	public void setHoursPerDay(int hoursPerDay) {
		this.hoursPerDay = hoursPerDay;
	}

	@Editable(order=200, description = "Specify working days per week. This will affect " +
			"parsing and displaying of working periods. For instance <tt>1w</tt> is the " +
			"same as <tt>5d</tt> if this property is set to <tt>5</tt>")
	@DependsOn(property="useHoursAndMinutesOnly", value="false")
	@Max(7)
	@Min(1)
	public int getDaysPerWeek() {
		return daysPerWeek;
	}

	public void setDaysPerWeek(int daysPerWeek) {
		this.daysPerWeek = daysPerWeek;
	}

	@Editable(order=500, placeholder = "No aggregation", description = "If specified, total estimated/spent time " +
			"of an issue will also include linked issues of this type")
	@ChoiceProvider("getLinkChoices")
	public String getAggregationLink() {
		return aggregationLink;
	}

	public void setAggregationLink(String aggregationLink) {
		this.aggregationLink = aggregationLink;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getLinkChoices() {
		var choices = new LinkedHashSet<String>();
		for (var linkSpec: OneDev.getInstance(LinkSpecService.class).query()) {
			if (linkSpec.getOpposite() != null) {
				choices.add(linkSpec.getName());
				choices.add(linkSpec.getOpposite().getName());
			}
		}
		return new ArrayList<>(choices);
	}

	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		if (linkName.equals(aggregationLink))
			usage.add(_T("time aggregation link"));
		return usage;
	}

	public void onRenameLink(String oldName, String newName) {
		if (oldName.equals(aggregationLink))
			aggregationLink = newName;
	}

	public int parseWorkingPeriod(String period) {
		period = StringUtils.deleteWhitespace(period);
		if (StringUtils.isBlank(period))
			throw new ValidationException(_T("Invalid working period"));

		if (period.equals("0"))
			return 0;

		if (useHoursAndMinutesOnly) {
			Matcher matcher = HOURS_AND_MINUTES_ONLY_WORKING_PERIOD_PATTERN.matcher(period);
			if (!matcher.matches())
				throw new ValidationException(_T("Invalid working period"));
	
			int minutes = 0;	
			if (matcher.group(1) != null) {
				int hours = Integer.parseInt(StringUtils.stripEnd(matcher.group(1), "h"));
				minutes += hours*60;
			}
	
			if (matcher.group(2) != null)
				minutes += Integer.parseInt(StringUtils.stripEnd(matcher.group(2), "m"));
	
			return minutes;	
		} else {
			Matcher matcher = WORKING_PERIOD_PATTERN.matcher(period);
			if (!matcher.matches())
				throw new ValidationException(_T("Invalid working period"));
	
			int minutes = 0;
			if (matcher.group(1) != null) {
				int weeks = Integer.parseInt(StringUtils.stripEnd(matcher.group(1), "w"));
				minutes += weeks * daysPerWeek * hoursPerDay * 60;
			}
	
			if (matcher.group(2) != null) {
				int days = Integer.parseInt(StringUtils.stripEnd(matcher.group(2), "d"));
				minutes += days * hoursPerDay * 60;
			}
	
			if (matcher.group(3) != null) {
				int hours = Integer.parseInt(StringUtils.stripEnd(matcher.group(3), "h"));
				minutes += hours*60;
			}
	
			if (matcher.group(4) != null)
				minutes += Integer.parseInt(StringUtils.stripEnd(matcher.group(4), "m"));
	
			return minutes;	
		}
	}

	public String formatWorkingPeriod(int minutes, boolean translate) {
		if (useHoursAndMinutesOnly) {
			int hours = minutes / 60;
			minutes = minutes % 60;

			StringBuilder builder = new StringBuilder();
			if (hours != 0)
				builder.append(MessageFormat.format(translate? _T("{0}h"): "{0}h", hours)).append(" ");
			if (minutes != 0)
				builder.append(MessageFormat.format(translate? _T("{0}m"): "{0}m", minutes));
	
			String formatted = builder.toString().trim();
			if (formatted.length() == 0)
				formatted = MessageFormat.format(translate? _T("{0}h"): "{0}h", 0);
			return formatted;	
		} else {
			int weeks = minutes / (60 * hoursPerDay * daysPerWeek);
			minutes = minutes % (60 * hoursPerDay * daysPerWeek);
			int days = minutes / (60 * hoursPerDay);
			minutes = minutes % (60 * hoursPerDay);
			int hours = minutes / 60;
			minutes = minutes % 60;
	
			StringBuilder builder = new StringBuilder();
			if (weeks != 0)
				builder.append(MessageFormat.format(translate? _T("{0}w"): "{0}w", weeks)).append(" ");
			if (days != 0)
				builder.append(MessageFormat.format(translate? _T("{0}d"): "{0}d", days)).append(" ");
			if (hours != 0)
				builder.append(MessageFormat.format(translate? _T("{0}h"): "{0}h", hours)).append(" ");
			if (minutes != 0)
				builder.append(MessageFormat.format(translate? _T("{0}m"): "{0}m", minutes));
	
			String formatted = builder.toString().trim();
			if (formatted.length() == 0)
				formatted = MessageFormat.format(translate? _T("{0}h"): "{0}h", 0);
			return formatted;	
		}
	}
	
	public String getWorkingPeriodHelp() {
		if (useHoursAndMinutesOnly) {
			return _T("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute");
		} else {
			return MessageFormat.format(_T("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute"), 
					daysPerWeek, hoursPerDay);
		}
	}
	
}
