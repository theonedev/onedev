package io.onedev.server.util.date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

public class Week implements Serializable, Comparable<Week> {

	private static final long serialVersionUID = 1L;

	private final int weekyear; 
	
	private final int weekOfWeekyear;
	
	public Week(Date date) {
		this(new DateTime(date));
	}
	
	public Week(DateTime dateTime) {
		this(dateTime.getWeekyear(), dateTime.getWeekOfWeekyear());
	}
	
	public Week(int weekyear, int weekOfWeekyear) {
		this.weekyear = weekyear;
		this.weekOfWeekyear = weekOfWeekyear;
	}
	
	public Week(int value) {
		this(value>>>16, value&0x0000ffff);
	}

	public int getValue() {
		return (weekyear<<16) | weekOfWeekyear;
	}
	
	public DateTime getDate() {
		return new DateTime().withWeekyear(weekyear).withWeekOfWeekyear(weekOfWeekyear).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0);
	}

	@Override
	public int compareTo(Week week) {
		if (weekyear < week.weekyear) 
			return -1;
		else if (weekyear > week.weekyear) 
			return 1;
		else 
			return weekOfWeekyear - week.weekOfWeekyear;
	}
	
	public int getWeekyear() {
		return weekyear;
	}

	public int getWeekOfWeekyear() {
		return weekOfWeekyear;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Week) {
			Week otherWeek = (Week) other;
			return new EqualsBuilder()
					.append(weekyear, otherWeek.weekyear)
					.append(weekOfWeekyear, otherWeek.weekOfWeekyear)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(weekyear)
				.append(weekOfWeekyear)
				.toHashCode();
	}

	@Override
	public String toString() {
		return String.format("w%02d", weekOfWeekyear);
	}
	
}
