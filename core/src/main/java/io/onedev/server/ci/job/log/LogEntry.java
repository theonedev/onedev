package io.onedev.server.ci.job.log;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final LogLevel level;
	
	private final String message;
	
	public LogEntry(Date date, LogLevel level, String message) {
		this.date = date;
		this.level = level;
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}
	
}
