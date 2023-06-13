package io.onedev.server.event.cluster;

import io.onedev.server.event.Event;

public abstract class ConnectionEvent extends Event {
	
	private final String server;
	
	public ConnectionEvent(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}
	
}
