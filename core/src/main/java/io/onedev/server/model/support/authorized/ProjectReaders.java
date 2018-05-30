package io.onedev.server.model.support.authorized;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.ProjectPrivilege;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=50, name="Users able to access the project")
public class ProjectReaders implements Authorized {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return user.asSubject().isPermitted(new ProjectPermission(project.getFacade(), ProjectPrivilege.READ));
	}

}
