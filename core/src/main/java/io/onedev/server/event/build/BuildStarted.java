package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public class BuildStarted extends BuildEvent {

	public BuildStarted(Build build) {
		super(build);
	}

}
