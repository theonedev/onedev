package io.onedev.server.logging;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import org.jetbrains.annotations.Nullable;

public class ServerLogger extends TaskLogger {
	
	private final String server;
	
	private final String token;
	
	public ServerLogger(String server, String token) {
		this.server = server;
		this.token = token;
	}
	
	@Override
	public void log(String message, @Nullable String sessionId) {
		var clusterService = OneDev.getInstance(ClusterService.class);
		clusterService.runOnServer(server, new LogTask(token, message, sessionId));	
	}
	
}
