package com.gitplex.server.util;

import java.io.Serializable;
import java.util.Date;

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
	
}
