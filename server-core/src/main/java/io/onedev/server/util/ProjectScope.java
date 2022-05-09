package io.onedev.server.util;

import io.onedev.server.model.Project;

public class ProjectScope {

	private final Project project;

	private final boolean inherited;
	
	private final boolean recursive;
	
	public ProjectScope(Project project, boolean inherited, boolean recursive) {
		this.project = project;
		this.inherited = inherited;
		this.recursive = recursive;
	}

	public Project getProject() {
		return project;
	}
	
	public boolean isInherited() {
		return inherited;
	}

	public boolean isRecursive() {
		return recursive;
	}
	
}
