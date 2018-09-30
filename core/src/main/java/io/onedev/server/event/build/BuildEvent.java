package io.onedev.server.event.build;

import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Build;

public abstract class BuildEvent extends ProjectEvent {

	private final Build build;

	public BuildEvent(Build build) {
		super(null, build.getDate(), build.getConfiguration().getProject());
		this.build = build;
	}

	public Build getBuild() {
		return build;
	}
	
}
