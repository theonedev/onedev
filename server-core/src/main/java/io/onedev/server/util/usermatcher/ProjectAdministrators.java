package io.onedev.server.util.usermatcher;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ProjectPrivilege;

public class ProjectAdministrators implements UserMatcherCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return user.asSubject().isPermitted(new ProjectPermission(project.getFacade(), ProjectPrivilege.ADMINISTRATION));
	}

	@Override
	public String toString() {
		return "project administrators";
	}
	
}
