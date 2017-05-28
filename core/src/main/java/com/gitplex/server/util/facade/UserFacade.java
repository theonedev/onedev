package com.gitplex.server.util.facade;

public class UserFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;
	
	public UserFacade(Long id, String name, String fullName) {
		super(id);
		
		this.name = name;
		this.fullName = fullName;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}
	
}
