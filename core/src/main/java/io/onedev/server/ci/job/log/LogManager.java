package io.onedev.server.ci.job.log;

import java.util.List;

import io.onedev.server.model.Build2;

public interface LogManager {
	
	JobLogger getLogger(Long projectId, Long buildId, LogLevel logLevel);
	
	List<LogEntry> readLogEntries(Build2 build, int from, int count);
	
}
