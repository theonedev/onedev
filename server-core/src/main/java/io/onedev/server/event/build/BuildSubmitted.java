package io.onedev.server.event.build;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildSubmitted extends BuildEvent {

	public BuildSubmitted(Build build) {
		super(build.getSubmitter(), build.getSubmitDate(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildSubmitted(dao.load(Build.class, getBuild().getId()));
	}
	
}