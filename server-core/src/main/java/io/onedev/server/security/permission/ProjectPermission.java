package io.onedev.server.security.permission;

import io.onedev.server.model.Project;
import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

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
	public boolean implies(Permission p) {
		if (p instanceof ProjectPermission) {
			ProjectPermission projectPermission = (ProjectPermission) p;
			return project.isSelfOrAncestorOf(projectPermission.project) 
					&& privilege.implies(projectPermission.privilege);
		} else {
			return false;
		}
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return privilege.isApplicable(user);
	}
}
