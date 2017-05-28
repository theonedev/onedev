package com.gitplex.server.util.facade;

public class GroupAuthorizationFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long groupId;
	
	private final Long projectId;
	
	public GroupAuthorizationFacade(Long id, Long groupId, Long projectId) {
		super(id);
	
		this.groupId = groupId;
		this.projectId = projectId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public Long getProjectId() {
		return projectId;
	}

}
