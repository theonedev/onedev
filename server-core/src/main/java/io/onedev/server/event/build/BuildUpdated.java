package io.onedev.server.event.build;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;

public class BuildUpdated extends BuildEvent {

	public BuildUpdated(Build build) {
		super(null, new Date(), build);
	}

	@Override
	public BuildEvent cloneIn(Dao dao) {
		return new BuildUpdated(dao.load(Build.class, getBuild().getId()));
	}
	
}
