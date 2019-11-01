package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

import io.onedev.server.model.Project;

public class ProjectPermission implements Permission {

	private final Project project;
	
	private final Permission privilege;
	
	public ProjectPermission(Project project, Permission privilege) {
		this.project = project;
		this.privilege = privilege;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof ProjectPermission) {
			ProjectPermission projectPermission = (ProjectPermission) p;
			return project.equals(projectPermission.project) && privilege.implies(projectPermission.privilege);
		} else {
			return false;
		}
	}

}
