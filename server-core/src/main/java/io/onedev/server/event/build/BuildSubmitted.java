package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public class BuildSubmitted extends BuildEvent {

	public BuildSubmitted(Build build) {
		super(build.getSubmitter(), build.getSubmitDate(), build);
	}

}