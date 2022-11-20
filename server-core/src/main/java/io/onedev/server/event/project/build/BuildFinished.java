package io.onedev.server.event.project.build;

import io.onedev.server.model.Build;

public class BuildFinished extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildFinished(Build build) {
		super(null, build.getFinishDate(), build);
	}

}
