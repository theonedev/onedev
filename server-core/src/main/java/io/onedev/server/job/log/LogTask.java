package io.onedev.server.job.log;

import javax.annotation.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;

public class LogTask implements ClusterTask<Void> {

	private static final long serialVersionUID = 1L;

	private final String jobToken;
	
	private final String message;
	
	private final String sessionId;
	
	public LogTask(String jobToken, String message, @Nullable String sessionId) {
		this.jobToken = jobToken;
		this.message = message;
		this.sessionId = sessionId;
	}
	
	@Override
	public Void call() {
		TaskLogger logger = OneDev.getInstance(LogManager.class).getJobLogger(jobToken);
		if (logger != null && !(logger instanceof ServerJobLogger))  
			logger.log(message, sessionId);
		return null;
	}
	
}