package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.model.PullRequestWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultPullRequestWatchManager implements PullRequestWatchManager {

	private final Dao dao;
	
	@Inject
	public DefaultPullRequestWatchManager(Dao dao) {
		this.dao = dao;
	}

	@Sessional
	@Override
	public Collection<PullRequestWatch> findBy(User user, Repository repository) {
		EntityCriteria<PullRequestWatch> criteria = EntityCriteria.of(PullRequestWatch.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.createCriteria("branch").add(Restrictions.eq("repository", repository));
		return dao.query(criteria);
	}

}
