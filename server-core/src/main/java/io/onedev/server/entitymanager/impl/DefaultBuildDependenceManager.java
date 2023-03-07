package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildDependenceManager extends BaseEntityManager<BuildDependence> implements BuildDependenceManager {

	@Inject
	public DefaultBuildDependenceManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void create(BuildDependence dependence) {
		Preconditions.checkState(dependence.isNew());
		dao.persist(dependence);
	}
	
}
