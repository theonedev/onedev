package com.turbodev.server.util.facade;

import com.turbodev.server.model.GroupAuthorization;
import com.turbodev.server.security.ProjectPrivilege;

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
