package io.onedev.server.event.project;

import javax.annotation.Nullable;
import java.io.Serializable;

public class ActiveServerChanged implements Serializable {
	
	private final Long projectId;
	
	private final String oldActiveServer;
	
	public ActiveServerChanged(Long projectId, @Nullable String oldActiveServer) {
		this.projectId = projectId;
		this.oldActiveServer = oldActiveServer;
	}

	public Long getProjectId() {
		return projectId;
	}

	@Nullable
	public String getOldActiveServer() {
		return oldActiveServer;
	}
}
