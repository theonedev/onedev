package io.onedev.server.event.cluster;

public class NodeDisconnected extends NodeEvent {
	
	private final boolean abnormal;

	public NodeDisconnected(String server, boolean abnormal) {
		super(server);
		this.abnormal = abnormal;
	}

	public boolean isAbnormal() {
		return abnormal;
	}

}
