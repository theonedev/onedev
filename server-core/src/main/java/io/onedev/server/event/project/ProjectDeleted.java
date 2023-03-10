package io.onedev.server.event.project;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

import java.io.Serializable;
import java.util.Date;

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
