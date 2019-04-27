package io.onedev.server.event.build;

import java.util.Date;

import io.onedev.server.model.Build;

public class BuildUpdated extends BuildEvent {

	public BuildUpdated(Build build) {
		super(null, new Date(), build);
	}

}
