package io.onedev.server.event.project.build;

import io.onedev.server.model.Build;

public class BuildRunning extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildRunning(Build build) {
		super(null, build.getRunningDate(), build);
	}

}
