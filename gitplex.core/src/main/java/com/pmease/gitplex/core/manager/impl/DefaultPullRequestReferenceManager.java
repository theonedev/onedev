package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.manager.PullRequestReferenceManager;

@Singleton
public class DefaultPullRequestReferenceManager extends AbstractEntityManager<PullRequestReference> 
		implements PullRequestReferenceManager {

	@Inject
	public DefaultPullRequestReferenceManager(Dao dao) {
		super(dao);
	}

}
