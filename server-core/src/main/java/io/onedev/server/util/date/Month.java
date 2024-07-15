package io.onedev.server.util.date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

public class Month implements Serializable, Comparable<Month> {

	private static final long serialVersionUID = 1L;

	private final int year; 
	
	private final int monthOfYear;
	
	public Month(Date date) {
		this(new DateTime(date));
	}
	
	public Month(DateTime dateTime) {
		this(dateTime.getYear(), dateTime.getMonthOfYear());
	}
	
	public Month(int year, int monthOfYear) {
		this.year = year;
		this.monthOfYear = monthOfYear;
	}
	
	public Month(int value) {
		this(value>>>16, value&0x0000ffff);
	}

	public int getValue() {
		return (year<<16) | monthOfYear;
	}
	
	public DateTime getDate() {
		return new DateTime(year, monthOfYear, 1, 0, 0);
	}

	@Override
	public int compareTo(Month month) {
		if (year < month.year) 
			return -1;
		else if (year > month.year) 
			return 1;
		else 
			return monthOfYear - month.monthOfYear;
	}
	
	public int getYear() {
		return year;
	}

	public int getMonthOfYear() {
		return monthOfYear;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Month) {
			Month otherDay = (Month) other;
			return new EqualsBuilder()
					.append(year, otherDay.year)
					.append(monthOfYear, otherDay.monthOfYear)
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
				.toHashCode();
	}

	@Override
	public String toString() {
		return String.format("%04d-%02d", year, monthOfYear);
	}
	
}
