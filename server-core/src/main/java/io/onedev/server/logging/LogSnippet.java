package io.onedev.server.logging;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class LogSnippet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public List<LogEntry> entries = new LinkedList<>();
	
	/**
	 * offset of first log entry in the snippet
	 */
	public int offset;
	
}