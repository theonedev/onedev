package io.onedev.server.event.project.build;

import java.io.Serial;

import io.onedev.server.model.Build;

public class BuildRunning extends BuildEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	public BuildRunning(Build build) {
		super(null, build.getRunningDate(), build);
	}

}
