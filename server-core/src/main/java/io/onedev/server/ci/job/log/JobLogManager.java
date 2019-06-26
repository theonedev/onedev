package io.onedev.server.ci.job.log;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import io.onedev.server.model.Build;

public interface JobLogManager {
	
	JobLogger getLogger(Build build, Collection<String> jobSecrets);
	
	/**
	 * Read specified number of log entries from specified build, starting from specified index 
	 * 
	 * @param build
	 * 			build to read log entries from
	 * @param offset
	 * 			index of the log entry to start read
	 * @param count
	 * 			number of log entries to read. Specifically use <tt>0</tt> to read all entries 
	 * 			since offset
	 * @return
	 * 			log entries. Number of entries may be less than required count if there is no 
	 * 			enough log entries
	 */
	List<JobLogEntry> readLogEntries(Build build, int offset, int count);
	
	/**
	 * Read specified number of log entries starting from end of the log
	 * 
	 * @param build
	 * 			build to read log entries from 
	 * @param count
	 * 			number of log entries to read
	 * @return
	 * 			log entries with normal order. Number of entries may be less than required count 
	 * 			if there is no enough log entries
	 */
	LogSnippet readLogSnippetReversely(Build build, int count);
	
	InputStream openLogStream(Build build);
	
}
