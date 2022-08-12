package io.onedev.server.event.build;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildRetrying extends BuildEvent {

	public BuildRetrying(Build build) {
		super(build.getSubmitter(), build.getRetryDate(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildRetrying(dao.load(Build.class, getBuild().getId()));
	}

}