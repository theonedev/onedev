package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequestVisit;
import com.pmease.gitplex.core.manager.PullRequestVisitManager;

@Singleton
public class DefaultPullRequestVisitManager 
		extends AbstractEntityDao<PullRequestVisit> implements PullRequestVisitManager {

	@Inject
	public DefaultPullRequestVisitManager(Dao dao) {
		super(dao);
	}

}
