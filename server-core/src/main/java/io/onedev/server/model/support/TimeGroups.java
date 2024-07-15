package io.onedev.server.model.support;

import io.onedev.server.util.date.Day;
import io.onedev.server.util.date.Hour;
import io.onedev.server.util.date.Month;
import io.onedev.server.util.date.Week;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class TimeGroups implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DAY = "day";
	
	public static final String PROP_WEEK = "week";
	
	public static final String PROP_MONTH = "month";
	
	private Integer day;

	private Integer week;
	
	private Integer month;

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}
	
	public static TimeGroups of(Date date) {
		var timeGroups = new TimeGroups();
		timeGroups.setMonth(new Month(date).getValue());
		timeGroups.setWeek(new Week(date).getValue());
		timeGroups.setDay(new Day(date).getValue());
		return timeGroups;
	}
}
