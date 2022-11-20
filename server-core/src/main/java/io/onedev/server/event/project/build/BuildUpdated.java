package io.onedev.server.event.project.build;

import java.util.Date;

import io.onedev.server.model.Build;

public class BuildUpdated extends BuildEvent {

	private static final long serialVersionUID = 1L;

	public BuildUpdated(Build build) {
		super(null, new Date(), build);
	}

}
