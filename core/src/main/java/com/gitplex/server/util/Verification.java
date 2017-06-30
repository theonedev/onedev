package com.gitplex.server.util;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

public class Verification implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Status {SUCCESS, FAILURE, ERROR, PENDING};
	
	private final Status status; 
	
	private final Date date;
	
	private final String description;
	
	private final String targetUrl;

	public Verification(Status status, Date date, @Nullable String description, @Nullable String targetUrl) {
		this.status = status;
		this.date = date;
		this.description = description;
		this.targetUrl = targetUrl;
	}

	public Date getDate() {
		return date;
	}

	public Status getStatus() {
		return status;
	}

	public String getDescription() {
		return description;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

}
