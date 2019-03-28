package io.onedev.server.event.build2;

import io.onedev.server.model.Build2;

public class BuildSubmitted extends BuildEvent {

	public BuildSubmitted(Build2 build) {
		super(build.getUser(), build.getSubmitDate(), build);
	}

}