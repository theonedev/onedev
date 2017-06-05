package com.gitplex.server.util.facade;

import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.security.ProjectPrivilege;

public class GroupAuthorizationFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long groupId;
	
	private final Long projectId;
	
	private final ProjectPrivilege privilege;
	
	public GroupAuthorizationFacade(GroupAuthorization groupAuthorization) {
		super(groupAuthorization.getId());

		groupId = groupAuthorization.getGroup().getId();
		projectId = groupAuthorization.getProject().getId();
		privilege = groupAuthorization.getPrivilege();
	}

	public Long getGroupId() {
		return groupId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public ProjectPrivilege getPrivilege() {
		return privilege;
	}

}
