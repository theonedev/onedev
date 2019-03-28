package io.onedev.server.event.build2;

import io.onedev.server.model.Build2;

public class BuildRunning extends BuildEvent {

	public BuildRunning(Build2 build) {
		super(null, build.getRunningDate(), build);
	}

}
