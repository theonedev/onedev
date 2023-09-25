package io.onedev.server.updatecheck;

import org.joda.time.DateTime;

import java.util.Date;

public class UpdateCheck {

	private final String newVersionStatus;
	
	private final Date date;
	
	public UpdateCheck(String newVersionStatus, Date date) {
		this.newVersionStatus = newVersionStatus;
		this.date = date;
	}

	public String getNewVersionStatus() {
		return newVersionStatus;
	}

	public Date getDate() {
		return date;
	}
	
	public boolean isOutdated() {
		return new DateTime(date).isBefore(new DateTime().minusDays(1));
	}
	
}
