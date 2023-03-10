package io.onedev.server.job.log;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import org.jetbrains.annotations.Nullable;

public class ServerJobLogger extends TaskLogger {
	
	private final String server;
	
	private final String jobToken;
	
	public ServerJobLogger(String server, String jobToken) {
		this.server = server;
		this.jobToken = jobToken;
	}
	
	@Override
	public void log(String message, @Nullable String sessionId) {
		var clusterManager = OneDev.getInstance(ClusterManager.class);
		clusterManager.runOnServer(server, new LogTask(jobToken, message, sessionId));	
	}
	
}
