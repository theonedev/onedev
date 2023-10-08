package io.onedev.server.event.project;

import io.onedev.server.model.Project;

import java.io.Serializable;

public class ProjectDeleted implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	public ProjectDeleted(Project project) {
		projectId = project.getId();
	}

	public Long getProjectId() {
		return projectId;
	}
	
}
