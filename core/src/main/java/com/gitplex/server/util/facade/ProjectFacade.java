package com.gitplex.server.util.facade;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long userId;
	
	private final String name;

	public ProjectFacade(Long id, Long userId, String name) {
		super(id);
		this.userId = userId;
		this.name = name;
	}
	
	public Long getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

}
