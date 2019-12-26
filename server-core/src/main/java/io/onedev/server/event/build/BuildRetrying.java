package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public class BuildRetrying extends BuildEvent {

	public BuildRetrying(Build build) {
		super(build.getSubmitter(), build.getRetryDate(), build);
	}

}