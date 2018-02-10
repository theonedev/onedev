package com.turbodev.server.security.permission;

import org.apache.shiro.authz.Permission;

import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.util.facade.ProjectFacade;

public class ProjectPermission implements Permission {

	private final ProjectFacade project;
	
	private final ProjectPrivilege privilege;
	
	public ProjectPermission(ProjectFacade project, ProjectPrivilege privilege) {
		this.project = project;
		this.privilege = privilege;
	}
	
	public ProjectFacade getProject() {
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
