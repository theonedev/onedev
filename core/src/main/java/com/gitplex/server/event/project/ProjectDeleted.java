package com.gitplex.server.event.project;

import com.gitplex.server.model.Project;

public class ProjectDeleted {
	
	private final Project project;
	
	public ProjectDeleted(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}
	
}
