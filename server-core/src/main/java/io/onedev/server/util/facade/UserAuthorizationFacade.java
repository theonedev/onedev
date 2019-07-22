package io.onedev.server.util.facade;

import io.onedev.server.model.UserAuthorization;
import io.onedev.server.security.permission.ProjectPrivilege;

public class UserAuthorizationFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long userId;
	
	private final Long projectId;
	
	private final ProjectPrivilege privilege;
	
	public UserAuthorizationFacade(UserAuthorization userAuthorization) {
		super(userAuthorization.getId());

		userId = userAuthorization.getUser().getId();
		projectId = userAuthorization.getProject().getId();
		privilege = userAuthorization.getPrivilege();
	}

	public Long getUserId() {
		return userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public ProjectPrivilege getPrivilege() {
		return privilege;
	}
	
}
