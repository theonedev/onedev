package com.gitplex.server.event.project;

import com.gitplex.server.model.Project;

public class ProjectRenamed {
	
	private final Project project;
	
	private final String oldName;
	
	public ProjectRenamed(Project project, String oldName) {
		this.project = project;
		this.oldName = oldName;
	}

	public Project getProject() {
		return project;
	}

	public String getOldName() {
		return oldName;
	}
	
}
