package io.onedev.server.event;

import io.onedev.server.model.Project;

public class ProjectCreated extends ProjectEvent {
	
	public ProjectCreated(Project project) {
		super(project.getOwner(), project.getCreateDate(), project);
	}

}
