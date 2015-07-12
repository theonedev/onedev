package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.model.BranchWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultBranchWatchManager implements BranchWatchManager {

	private final Dao dao;
	
	@Inject
	public DefaultBranchWatchManager(Dao dao) {
		this.dao = dao;
	}

	@Sessional
	@Override
	public Collection<BranchWatch> findBy(User user, Repository repository) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("repository", repository));
		return dao.query(criteria);
	}

	@Override
	public Collection<BranchWatch> findBy(Repository repository, String branch) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("repository", repository));
		criteria.add(Restrictions.eq("branch", branch));
		return dao.query(criteria);
	}

}
