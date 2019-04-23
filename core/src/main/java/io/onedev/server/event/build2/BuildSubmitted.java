package io.onedev.server.event.build2;

import io.onedev.server.model.Build;

public class BuildSubmitted extends BuildEvent {

	public BuildSubmitted(Build build) {
		super(build.getUser(), build.getSubmitDate(), build);
	}

}