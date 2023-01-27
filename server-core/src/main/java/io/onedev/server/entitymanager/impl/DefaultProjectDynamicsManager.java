package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.ProjectDynamicsManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectCreated;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectDynamics;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class DefaultProjectDynamicsManager extends BaseEntityManager<ProjectDynamics> implements ProjectDynamicsManager {

	@Inject
	public DefaultProjectDynamicsManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Listen
	public void on(ProjectEvent event) {
		Project project = event.getProject();
		if (event instanceof RefUpdated) {
			project.getDynamics().setLastActivityDate(new Date());
			project.getDynamics().setLastCommitDate(new Date());
		} else if (!(event instanceof ProjectCreated) 
				&& event.getUser() != null 
				&& !event.getUser().isSystem()) {
			project.getDynamics().setLastActivityDate(new Date());
		}
	}
	
}
