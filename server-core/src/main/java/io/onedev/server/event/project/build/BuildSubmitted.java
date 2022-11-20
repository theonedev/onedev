package io.onedev.server.event.project.build;

import io.onedev.server.model.Build;

public class BuildSubmitted extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildSubmitted(Build build) {
		super(build.getSubmitter(), build.getSubmitDate(), build);
	}
	
}