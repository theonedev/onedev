package io.onedev.server.event.build;

import io.onedev.server.model.Build;

public class BuildFinished extends BuildEvent {

	public BuildFinished(Build build) {
		super(null, build.getFinishDate(), build);
	}

}
