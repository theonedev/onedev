package io.onedev.server.util;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

public class Day implements Serializable, Comparable<Day> {

	private static final long serialVersionUID = 1L;

	private final int year; 
	
	private final int monthOfYear;
	
	private final int dayOfMonth;
	
	public Day(Date date) {
		this(new DateTime(date));
	}
	
	public Day(DateTime dateTime) {
		this(dateTime.getYear(), dateTime.getMonthOfYear()-1, dateTime.getDayOfMonth());
	}
	
	public Day(int year, int monthOfYear, int dayOfMonth) {
		this.year = year;
		this.monthOfYear = monthOfYear;
		this.dayOfMonth = dayOfMonth;
	}
	
	public Day(int value) {
		this(value>>>16, (value&0x0000ffff)>>>8, value&0x000000ff);
	}

	public int getValue() {
		return (year<<16) | (monthOfYear<<8) | dayOfMonth;
	}
	
	public DateTime getDate() {
		return new DateTime(year, monthOfYear+1, dayOfMonth, 0, 0);
	}

	@Override
	public int compareTo(Day day) {
		if (year < day.year) 
			return -1;
		else if (year > day.year) 
			return 1;
		else if (monthOfYear < day.monthOfYear)
			return -1;
		else if (monthOfYear > day.monthOfYear)
			return 1;
		else 
			return dayOfMonth - day.dayOfMonth;
	}
	
	public int getYear() {
		return year;
	}

	public int getMonthOfYear() {
		return monthOfYear;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Day) {
			Day otherDay = (Day) other;
			return new EqualsBuilder()
					.append(year, otherDay.year)
					.append(monthOfYear, otherDay.monthOfYear)
					.append(dayOfMonth, otherDay.dayOfMonth)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(year)
				.append(monthOfYear)
				.append(dayOfMonth)
				.toHashCode();
	}
	
}
