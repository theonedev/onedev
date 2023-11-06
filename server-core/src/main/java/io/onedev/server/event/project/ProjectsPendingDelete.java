package io.onedev.server.event.project;

import io.onedev.server.event.Event;
import io.onedev.server.model.Project;

import java.io.Serializable;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class ProjectsPendingDelete extends Event implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Collection<Long> projectIds;
	
	public ProjectsPendingDelete(Collection<Project> projects) {
		projectIds = projects.stream().map(Project::getId).collect(toList());
	}

	public Collection<Long> getProjectIds() {
		return projectIds;
	}
	
}
