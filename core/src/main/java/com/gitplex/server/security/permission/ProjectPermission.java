package com.gitplex.server.security.permission;

import org.apache.shiro.authz.Permission;

import com.gitplex.server.model.Project;
import com.gitplex.server.security.ProjectPrivilege;

public class ProjectPermission implements Permission {

	private final Project project;
	
	private final ProjectPrivilege privilege;
	
	public ProjectPermission(Project project, ProjectPrivilege privilege) {
		this.project = project;
		this.privilege = privilege;
	}
	
	public Project getProject() {
		return project;
	}

	public ProjectPrivilege getPrivilege() {
		return privilege;
	}

	@Override
	public boolean implies(Permission p) {
		if (p instanceof ProjectPermission) {
			ProjectPermission projectPermission = (ProjectPermission) p;
			return project.equals(projectPermission.getProject()) && privilege.implies(projectPermission.getPrivilege());
		} else {
			return p instanceof PublicPermission;
		}
	}

}
