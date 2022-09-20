package io.onedev.server.event.build;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildResumed extends BuildEvent {

	public BuildResumed(Build build) {
		super(null, new Date(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildResumed(dao.load(Build.class, getBuild().getId()));
	}
	
}
