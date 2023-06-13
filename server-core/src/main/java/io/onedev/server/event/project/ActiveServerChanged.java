package io.onedev.server.event.project;

import java.io.Serializable;
import java.util.Collection;

public class ActiveServerChanged implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String activeServer;

	private final Collection<Long> projectIds;
	
	public ActiveServerChanged(String activeServer, Collection<Long> projectIds) {
		this.activeServer = activeServer;
		this.projectIds = projectIds;
	}

	public String getActiveServer() {
		return activeServer;
	}

	public Collection<Long> getProjectIds() {
		return projectIds;
	}

}
