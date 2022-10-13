package io.onedev.server.event;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

public class BuildDeleted extends ProjectEvent {

	private final Build build;
	
	public BuildDeleted(Build build) {
		super(SecurityUtils.getUser(), new Date(), build.getProject());
		this.build = build;
	}
	
	public Build getBuild() {
		return build;
	}
	
	@Override
	public String getActivity() {
		return "build deleted";
	}

	@Override
	public ProjectEvent cloneIn(Dao dao) {
		return new BuildDeleted(dao.load(Build.class, build.getId()));
	}

}
