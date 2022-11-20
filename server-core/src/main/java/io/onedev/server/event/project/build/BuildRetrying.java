package io.onedev.server.event.project.build;

import io.onedev.server.model.Build;

public class BuildRetrying extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildRetrying(Build build) {
		super(build.getSubmitter(), build.getRetryDate(), build);
	}

}