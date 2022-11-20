package io.onedev.server.event.project.build;

import io.onedev.server.model.Build;

public class BuildPending extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildPending(Build build) {
		super(null, build.getPendingDate(), build);
	}

}