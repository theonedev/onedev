package io.onedev.server.util.facade;

import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.security.permission.ProjectPrivilege;

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
