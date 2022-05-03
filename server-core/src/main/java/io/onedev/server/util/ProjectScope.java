package io.onedev.server.util;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.Project;

public class ProjectScope {
	
	private final Project project;
	
	private final boolean recursive;
	
	private final RecursiveConfigurable recursiveConfigurable;
	
	public ProjectScope(Project project, boolean recursive, RecursiveConfigurable recursiveConfigurable) {
		this.project = project;
		this.recursive = recursive;
		this.recursiveConfigurable = recursiveConfigurable;
	}

	public Project getProject() {
		return project;
	}
	
	public boolean isRecursive() {
		return recursive;
	}
	
	@Nullable
	public RecursiveConfigurable getRecursiveConfigurable() {
		return recursiveConfigurable;
	}

	public static interface RecursiveConfigurable {
		
		public void setRecursive(AjaxRequestTarget target, boolean recursive);
		
	}
	
}
