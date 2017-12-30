package com.gitplex.server.util;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

public class Day implements Serializable, Comparable<Day> {

	private static final long serialVersionUID = 1L;

	private final DateTime dateTime;
	
	public Day(Date date) {
		this(new DateTime(date));
	}
	
	public Day(DateTime dateTime) {
		this.dateTime = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 
				0, 0, 0, 0);
	}
	
	public Day(int year, int monthOfYear, int dayOfMonth) {
		dateTime = new DateTime(year, monthOfYear, dayOfMonth, 0, 0, 0, 0);
	}
	
	public Day(int value) {
		this(value>>>16, (value&0x0000ffff)>>>8, value&0x000000ff);
	}

	public Date getDate() {
		return dateTime.toDate();
	}
	
	public int getValue() {
		return (dateTime.getYear()<<16) | (dateTime.getMonthOfYear()<<8) | (dateTime.getDayOfMonth());
	}

	@Override
	public int compareTo(Day o) {
		return dateTime.compareTo(o.dateTime);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Day) {
			Day otherDay = (Day) other;
			return new EqualsBuilder().append(dateTime, otherDay.dateTime).isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(dateTime).toHashCode();
	}
	
}
