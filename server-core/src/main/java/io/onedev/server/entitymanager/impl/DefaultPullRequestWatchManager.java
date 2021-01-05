package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestWatchManager extends BaseEntityManager<PullRequestWatch> 
		implements PullRequestWatchManager {

	@Inject
	public DefaultPullRequestWatchManager(Dao dao) {
		super(dao);
	}

	@Override
	public PullRequestWatch find(PullRequest request, User user) {
		EntityCriteria<PullRequestWatch> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
		criteria.add(Restrictions.eq("user", user));
		return find(criteria);
	}

	@Override
	public void watch(PullRequest request, User user, boolean watching) {
		PullRequestWatch watch = (PullRequestWatch) request.getWatch(user, true);
		if (watch.isNew()) {
			watch.setWatching(watching);
			save(watch);
		}
	}
	
}
