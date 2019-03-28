package io.onedev.server.event.build2;

import io.onedev.server.model.Build2;

public class BuildFinished extends BuildEvent {

	public BuildFinished(Build2 build) {
		super(null, build.getFinishDate(), build);
	}

}
