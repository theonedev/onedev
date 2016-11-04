package com.gitplex.server.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.BranchWatch;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.manager.BranchWatchManager;

@Singleton
public class DefaultBranchWatchManager extends AbstractEntityManager<BranchWatch> implements BranchWatchManager {

	@Inject
	public DefaultBranchWatchManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public Collection<BranchWatch> find(Account user, Depot depot) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("depot", depot));
		return findAll(criteria);
	}

	@Override
	public Collection<BranchWatch> find(Depot depot, String branch) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("depot", depot));
		criteria.add(Restrictions.eq("branch", branch));
		return findAll(criteria);
	}

}
