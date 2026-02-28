package io.onedev.server.logging;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;

public interface LogService {
		
	TaskLogger newLogger(LoggingSupport loggingSupport);
	
	/**
	 * Read specified number of log entries from specified index 
	 * 
	 * @param offset
	 * 			index of the log entry to start read
	 * @param count
	 * 			number of log entries to read. Specifically use <tt>0</tt> to read all entries 
	 * 			since offset
	 * @return
	 * 			log entries. Number of entries may be less than required count if there is no 
	 * 			enough log entries
	 */
	List<JobLogEntryEx> readLogEntries(LoggingSupport loggingSupport, int offset, int count);
	
	void registerListener(LogListener listener);
	
	void deregisterListener(LogListener listener);
	
	boolean matches(LoggingSupport loggingSupport, Pattern pattern);
	
	void flush(LoggingSupport loggingSupport);

	/**
	 * Read specified number of log entries starting from end of the log
	 * 
	 * @return
	 * 			log entries with normal order. Number of entries may be less than required count 
	 * 			if there is no enough log entries
	 */
	LogSnippet readLogSnippetReversely(LoggingSupport loggingSupport, int count);
	
	InputStream openLogStream(LoggingIdentity loggingIdentity);
	
	@Nullable
	TaskLogger getLogger(String token);
	
	void addLogger(String token, TaskLogger logger);
	
	void removeLogger(String token);
	
}
