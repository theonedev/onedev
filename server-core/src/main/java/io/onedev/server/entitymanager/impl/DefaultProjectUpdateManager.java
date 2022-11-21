package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.ProjectUpdateManager;
import io.onedev.server.model.ProjectUpdate;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultProjectUpdateManager extends BaseEntityManager<ProjectUpdate> implements ProjectUpdateManager {

	@Inject
	public DefaultProjectUpdateManager(Dao dao) {
		super(dao);
	}

}
