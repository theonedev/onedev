package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.server.manager.PullRequestTaskManager;
import com.turbodev.server.model.PullRequestTask;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestTaskManager extends AbstractEntityManager<PullRequestTask> implements PullRequestTaskManager {
	
	@Inject
	public DefaultPullRequestTaskManager(Dao dao) {
		super(dao);
	}

}