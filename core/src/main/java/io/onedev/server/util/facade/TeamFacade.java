package io.onedev.server.util.facade;

import io.onedev.server.model.Team;
import io.onedev.server.security.permission.ProjectPrivilege;

public class TeamFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final String name;
	
	private ProjectPrivilege privilege;
	
	public TeamFacade(Team team) {
		super(team.getId());
		projectId = team.getProject().getId();
		name = team.getName();
		privilege = team.getPrivilege();
	}

	public String getName() {
		return name;
	}

	public Long getProjectId() {
		return projectId;
	}

	public ProjectPrivilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(ProjectPrivilege privilege) {
		this.privilege = privilege;
	}

}
