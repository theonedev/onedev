package io.onedev.server.event.cluster;

public class ConnectionRestored extends ConnectionEvent {
	
	public ConnectionRestored(String server) {
		super(server);
	}

}
