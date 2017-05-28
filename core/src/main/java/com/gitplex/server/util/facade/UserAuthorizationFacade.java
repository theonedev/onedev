package com.gitplex.server.util.facade;

public class UserAuthorizationFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long userId;
	
	private final Long projectId;
	
	public UserAuthorizationFacade(Long id, Long userId, Long projectId) {
		super(id);
		
		this.userId = userId;
		this.projectId = projectId;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getProjectId() {
		return projectId;
	}
	
}
