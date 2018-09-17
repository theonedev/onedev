package io.onedev.server.util.facade;

import io.onedev.server.model.Configuration;

public class ConfigurationFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final String name;
	
	public ConfigurationFacade(Configuration configuration) {
		super(configuration.getId());
		projectId = configuration.getProject().getId();
		name = configuration.getName();
	}
	
	public Long getProjectId() {
		return projectId;
	}

	public String getName() {
		return name;
	}

}
