package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import io.onedev.server.manager.PullRequestBuildManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestBuildManager extends AbstractEntityManager<PullRequestBuild> 
		implements PullRequestBuildManager {

	@Inject
	public DefaultPullRequestBuildManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void saveBuilds(PullRequest request) {
		Collection<Long> ids = new HashSet<>();
		for (PullRequestBuild build: request.getBuilds()) {
			save(build);
			ids.add(build.getId());
		}
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery("delete from PullRequestBuild where request=:request and id not in (:ids)");
			query.setParameter("request", request);
			query.setParameter("ids", ids);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from PullRequestBuild where request=:request");
			query.setParameter("request", request);
			query.executeUpdate();
		}
	}

}
