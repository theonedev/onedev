package io.onedev.server.event;

import java.util.Date;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

public class ProjectDeleted extends Event {
	
	private final Project project;
	
	public ProjectDeleted(Project project) {
		super(SecurityUtils.getUser(), new Date());
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

}
