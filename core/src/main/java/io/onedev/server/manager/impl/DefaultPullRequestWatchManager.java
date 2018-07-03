package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.PullRequestWatchManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestWatchManager extends AbstractEntityManager<PullRequestWatch> 
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
	
}
