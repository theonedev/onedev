package io.onedev.server.event.build2;

import io.onedev.server.model.Build2;

public class BuildPending extends BuildEvent {

	public BuildPending(Build2 build) {
		super(null, build.getPendingDate(), build);
	}

}