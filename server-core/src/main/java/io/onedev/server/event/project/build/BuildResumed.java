package io.onedev.server.event.project.build;

import java.util.Date;

import io.onedev.server.model.Build;

public class BuildResumed extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildResumed(Build build) {
		super(null, new Date(), build);
	}

}
