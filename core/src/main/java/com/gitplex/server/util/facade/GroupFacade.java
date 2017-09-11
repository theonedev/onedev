package com.gitplex.server.util.facade;

import com.gitplex.server.model.Group;

public class GroupFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final boolean administrator;
	
	private final boolean canCreateProjects;
	
	public GroupFacade(Group group) {
		super(group.getId());
		name = group.getName();
		administrator = group.isAdministrator();
		canCreateProjects = group.isCanCreateProjects();
	}

	public String getName() {
		return name;
	}

	public boolean isAdministrator() {
		return administrator;
	}

	public boolean isCanCreateProjects() {
		return canCreateProjects;
	}
	
}
