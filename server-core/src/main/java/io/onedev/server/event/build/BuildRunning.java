package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public class BuildRunning extends BuildEvent {

	public BuildRunning(Build build) {
		super(null, build.getRunningDate(), build);
	}

}
