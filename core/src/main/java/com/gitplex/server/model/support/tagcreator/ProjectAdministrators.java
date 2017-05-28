package com.gitplex.server.model.support.tagcreator;

import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=200, name="Repository Administrators")
public class ProjectAdministrators implements TagCreator {

	private static final long serialVersionUID = 1L;

	@Override
	public String getNotMatchMessage(Project project, User user) {
		if (!user.asSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.ADMIN))) {
			return "This operation can only be performed by project administrators";
		} else {
			return null;
		}
	}

}
