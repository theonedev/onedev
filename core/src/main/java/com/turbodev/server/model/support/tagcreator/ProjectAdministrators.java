package com.turbodev.server.model.support.tagcreator;

import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.security.permission.ProjectPermission;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(order=200, name="Project Administrators")
public class ProjectAdministrators implements TagCreator {

	private static final long serialVersionUID = 1L;

	@Override
	public String getNotMatchMessage(Project project, User user) {
		if (!user.asSubject().isPermitted(new ProjectPermission(project.getFacade(), ProjectPrivilege.ADMIN))) {
			return "This operation can only be performed by project administrators";
		} else {
			return null;
		}
	}

}
