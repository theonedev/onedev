package io.onedev.server.util.date;

import io.onedev.server.rest.annotation.Api;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class Hour implements Serializable, Comparable<Hour> {

	private static final long serialVersionUID = 1L;

	private final int year; 
	
	private final int monthOfYear;
	
	private final int dayOfMonth;
	
	private final int hourOfDay;
	
	public Hour(Date date) {
		this(new DateTime(date));
	}
	
	public Hour(DateTime dateTime) {
		this(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay());
	}
	
	public Hour(int year, int monthOfYear, int dayOfMonth, int hourOfDay) {
		this.year = year;
		this.monthOfYear = monthOfYear;
		this.dayOfMonth = dayOfMonth;
		this.hourOfDay = hourOfDay;
	}
	
	public Hour(int value) {
		this(value>>>16, (value&0x0000ffff)>>>12, (value&0x00000fff)>>>6, value&0x0000003f);
	}

	public int getValue() {
		return (year<<16) | (monthOfYear<<12) | (dayOfMonth<<6) | hourOfDay;
	}
	
	public DateTime getDate() {
		return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, 0);
	}

	@Override
	public int compareTo(Hour hour) {
		if (year < hour.year) 
			return -1;
		else if (year > hour.year) 
			return 1;
		else if (monthOfYear < hour.monthOfYear)
			return -1;
		else if (monthOfYear > hour.monthOfYear)
			return 1;
		else if (dayOfMonth < hour.dayOfMonth)
			return -1;
		else if (dayOfMonth > hour.dayOfMonth)
			return 1;
		else 
			return hourOfDay - hour.hourOfDay;
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

	public int getHourOfDay() {
		return hourOfDay;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Hour) {
			Hour otherHour = (Hour) other;
			return new EqualsBuilder()
					.append(year, otherHour.year)
					.append(monthOfYear, otherHour.monthOfYear)
					.append(dayOfMonth, otherHour.dayOfMonth)
					.append(hourOfDay, otherHour.hourOfDay)
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
				.append(hourOfDay)
				.toHashCode();
	}

	@Override
	public String toString() {
		return String.format("%02d", hourOfDay);
	}
	
}
