package io.onedev.server.util.usermatcher;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ProjectPrivilege;

public class IssueReaders implements UserMatcherCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return user.asSubject().isPermitted(new ProjectPermission(project.getFacade(), ProjectPrivilege.ISSUE_READ));
	}

	@Override
	public String toString() {
		return "issue readers";
	}
	
}
