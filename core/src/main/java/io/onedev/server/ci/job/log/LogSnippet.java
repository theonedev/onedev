package io.onedev.server.ci.job.log;

import java.util.LinkedList;
import java.util.List;

public class LogSnippet {
	
	public List<LogEntry> entries = new LinkedList<>();
	
	/**
	 * offset of first log entry in the snippet
	 */
	public int offset;
	
}