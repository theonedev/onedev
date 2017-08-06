package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.PullRequestTaskManager;
import com.gitplex.server.model.PullRequestTask;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestTaskManager extends AbstractEntityManager<PullRequestTask> implements PullRequestTaskManager {
	
	@Inject
	public DefaultPullRequestTaskManager(Dao dao) {
		super(dao);
	}

}