package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.ProjectUpdateManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectCreated;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.model.ProjectUpdate;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class DefaultProjectUpdateManager extends BaseEntityManager<ProjectUpdate> implements ProjectUpdateManager {

	@Inject
	public DefaultProjectUpdateManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof RefUpdated 
				|| !(event instanceof ProjectCreated) && event.getUser() != null && !event.getUser().isSystem()) {
			event.getProject().getUpdate().setDate(new Date());
		}
	}
	
}
