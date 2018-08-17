package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public abstract class BuildEvent {

	private final Build build;

	public BuildEvent(Build build) {
		this.build = build;
	}

	public Build getBuild() {
		return build;
	}
	
}
