package io.onedev.server.event.cluster;

import io.onedev.server.event.Event;

public abstract class NodeEvent extends Event {
	
	private final String server;
	
	public NodeEvent(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}
	
}
