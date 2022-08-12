package io.onedev.server.event.build;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildRunning extends BuildEvent {

	public BuildRunning(Build build) {
		super(null, build.getRunningDate(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildRunning(dao.load(Build.class, getBuild().getId()));
	}
	
}
