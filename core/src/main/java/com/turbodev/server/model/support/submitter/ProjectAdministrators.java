package com.turbodev.server.model.support.submitter;

import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.security.permission.ProjectPermission;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(order=400, name="Project administrators")
public class ProjectAdministrators implements Submitter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return user.asSubject().isPermitted(new ProjectPermission(project.getFacade(), ProjectPrivilege.ADMIN));
	}

}
