package io.onedev.server.model.support;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import javax.persistence.Embeddable;

import io.onedev.server.util.DateUtils;

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
		var localDate = DateUtils.toLocalDate(date, ZoneId.systemDefault());
		timeGroups.setMonth((int) localDate.with(TemporalAdjusters.firstDayOfMonth()).toEpochDay());
		timeGroups.setWeek((int) localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay());
		timeGroups.setDay((int) localDate.toEpochDay());
		return timeGroups;
	}
}
