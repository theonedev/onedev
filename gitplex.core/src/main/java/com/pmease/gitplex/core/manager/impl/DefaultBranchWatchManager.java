package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.BranchWatchManager;

@Singleton
public class DefaultBranchWatchManager extends AbstractEntityDao<BranchWatch> implements BranchWatchManager {

	@Inject
	public DefaultBranchWatchManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public Collection<BranchWatch> findBy(Account user, Depot depot) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("depot", depot));
		return query(criteria);
	}

	@Override
	public Collection<BranchWatch> findBy(Depot depot, String branch) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("depot", depot));
		criteria.add(Restrictions.eq("branch", branch));
		return query(criteria);
	}

}
