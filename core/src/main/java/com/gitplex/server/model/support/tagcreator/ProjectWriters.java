package com.gitplex.server.model.support.tagcreator;

import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=100, name="All Users Able to Write to the Repository")
public class ProjectWriters implements TagCreator {

	private static final long serialVersionUID = 1L;

	@Override
	public String getNotMatchMessage(Project project, User user) {
		if (!user.asSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.WRITE))) {
			return "This operation can only be performed by project writers";
		} else {
			return null;
		}
	}

}
