package io.onedev.server.event;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

public class ProjectCreated extends ProjectEvent {
	
	public ProjectCreated(Project project) {
		super(SecurityUtils.getUser(), project.getCreateDate(), project);
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "created";
		if (withEntity)
			activity += " project " + getProject().getName();
		return activity;
	}

}
