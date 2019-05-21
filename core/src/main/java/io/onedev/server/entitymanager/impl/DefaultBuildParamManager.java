package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildParamManager extends AbstractEntityManager<BuildParam> implements BuildParamManager {

	@Inject
	public DefaultBuildParamManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void deleteParams(Build build) {
		Query query = getSession().createQuery("delete from BuildParam where build = :build");
		query.setParameter("build", build);
		query.executeUpdate();
		build.getParams().clear();
	}

}
