package com.gitplex.server.util.facade;

import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.security.ProjectPrivilege;

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
