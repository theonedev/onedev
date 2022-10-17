package io.onedev.server.job.log;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import io.onedev.server.buildspec.job.log.JobLogEntryEx;

public class LogSnippet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public List<JobLogEntryEx> entries = new LinkedList<>();
	
	/**
	 * offset of first log entry in the snippet
	 */
	public int offset;
	
}