package com.pmease.gitop.core.model.projectpermission;

import com.pmease.gitop.core.model.User;

/**
 * This interface serves as a mark up interface to indicate that all permissions
 * implementing this interface are project level permissions. 
 * 
 * @author robin
 *
 */
public class ProjectPermission {
	
	private User project;
	
	private ProjectOperation operation;

	public ProjectPermission(User project, ProjectOperation operation) {
		this.project = project;
		this.operation = operation;
	}
	
	public User getProject() {
		return project;
	}

	public void setProject(User project) {
		this.project = project;
	}

	public ProjectOperation getOperation() {
		return operation;
	}

	public void setOperation(ProjectOperation operation) {
		this.operation = operation;
	}
	
}
