package io.onedev.server.buildspec.job.log;

import java.util.LinkedList;
import java.util.List;

public class LogSnippet {
	
	public List<JobLogEntryEx> entries = new LinkedList<>();
	
	/**
	 * offset of first log entry in the snippet
	 */
	public int offset;
	
}