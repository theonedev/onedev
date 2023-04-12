package io.onedev.server.event.cluster;

public abstract class ConnectionEvent {
	
	private final String server;
	
	public ConnectionEvent(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}
	
}
