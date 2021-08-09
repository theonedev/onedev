package io.onedev.server.tasklog;

import java.util.LinkedList;
import java.util.List;

import io.onedev.server.buildspec.job.log.JobLogEntryEx;

public class LogSnippet {
	
	public List<JobLogEntryEx> entries = new LinkedList<>();
	
	/**
	 * offset of first log entry in the snippet
	 */
	public int offset;
	
}