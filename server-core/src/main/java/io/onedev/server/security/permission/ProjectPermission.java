package io.onedev.server.security.permission;

import io.onedev.server.model.Project;
import org.apache.shiro.authz.Permission;

public class ProjectPermission implements BasePermission {

	private final Project project;
	
	private final BasePermission privilege;
	
	public ProjectPermission(Project project, BasePermission privilege) {
		this.project = project;
		this.privilege = privilege;
	}

	public Project getProject() {
		return project;
	}

	public Permission getPrivilege() {
		return privilege;
	}

	@Override
	public boolean isWrite() {
		return privilege.isWrite();
	}

	@Override
	public boolean implies(Permission p) {
		if (p instanceof ProjectPermission) {
			ProjectPermission projectPermission = (ProjectPermission) p;
			return project.isSelfOrSettingsAncestorOf(projectPermission.project)
					&& privilege.implies(projectPermission.privilege);
		} else {
			return false;
		}
	}
}
