package io.onedev.server.event.build;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildPending extends BuildEvent {

	public BuildPending(Build build) {
		super(null, build.getPendingDate(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildPending(dao.load(Build.class, getBuild().getId()));
	}

}