package io.onedev.server.logging;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;

public class LogTask implements ClusterTask<Void> {

	private static final long serialVersionUID = 1L;

	private final String token;
	
	private final String message;
	
	private final String sessionId;
	
	public LogTask(String token, String message, @Nullable String sessionId) {
		this.token = token;
		this.message = message;
		this.sessionId = sessionId;
	}
	
	@Override
	public Void call() {
		TaskLogger logger = OneDev.getInstance(LogService.class).getLogger(token);
		if (logger != null && !(logger instanceof ServerLogger))  
			logger.log(message, sessionId);
		return null;
	}
	
}