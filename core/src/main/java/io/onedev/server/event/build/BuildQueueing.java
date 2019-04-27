package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public class BuildQueueing extends BuildEvent {

	public BuildQueueing(Build build) {
		super(null, build.getQueueingDate(), build);
	}

}