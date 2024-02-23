package io.onedev.server.job.log;

public interface LogListener {
	
	void logged(Long buildId);
	
}
