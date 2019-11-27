package io.onedev.server.buildspec.job.log;

import java.io.Serializable;
import java.util.Date;

public class JobLogEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final String message;
	
	public JobLogEntry(Date date, String message) {
		this.date = date;
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}
	
}
