package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.BuildParam;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildParamManager extends AbstractEntityManager<BuildParam> implements BuildParamManager {

	@Inject
	public DefaultBuildParamManager(Dao dao) {
		super(dao);
	}

}
