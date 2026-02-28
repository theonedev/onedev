package io.onedev.server.event.cluster;

public class NodeConnected extends NodeEvent {

	private final boolean recovered;

	public NodeConnected(String server, boolean recovered) {
		super(server);
		this.recovered = recovered;
	}

	public boolean isRecovered() {
		return recovered;
	}

}
