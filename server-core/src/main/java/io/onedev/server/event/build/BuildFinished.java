package io.onedev.server.event.build;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildFinished extends BuildEvent {

	public BuildFinished(Build build) {
		super(null, build.getFinishDate(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildFinished(dao.load(Build.class, getBuild().getId()));
	}

}
