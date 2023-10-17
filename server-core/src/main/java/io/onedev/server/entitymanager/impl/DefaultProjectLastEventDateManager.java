package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.ProjectLastEventDateManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectCreated;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastEventDate;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class DefaultProjectLastEventDateManager extends BaseEntityManager<ProjectLastEventDate> implements ProjectLastEventDateManager {

	@Inject
	public DefaultProjectLastEventDateManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Listen
	public void on(ProjectEvent event) {
		Project project = event.getProject();
		if (event instanceof RefUpdated) {
			project.getLastEventDate().setActivity(new Date());
			project.getLastEventDate().setCommit(new Date());
		} else if (!(event instanceof ProjectCreated) 
				&& event.getUser() != null 
				&& !event.getUser().isSystem()) {
			project.getLastEventDate().setActivity(new Date());
		}
	}
	
	@Transactional
	@Override
	public void create(ProjectLastEventDate lastEventDate) {
		Preconditions.checkState(lastEventDate.isNew());
		dao.persist(lastEventDate);
	}
	
}
