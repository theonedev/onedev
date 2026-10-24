package io.onedev.server.event.project.build;

import java.io.Serial;

import io.onedev.server.model.Build;

public class BuildPending extends BuildEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	public BuildPending(Build build) {
		super(null, build.getPendingDate(), build);
	}

}