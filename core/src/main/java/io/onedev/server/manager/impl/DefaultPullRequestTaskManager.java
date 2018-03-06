package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.PullRequestTaskManager;
import io.onedev.server.model.PullRequestTask;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestTaskManager extends AbstractEntityManager<PullRequestTask> implements PullRequestTaskManager {
	
	@Inject
	public DefaultPullRequestTaskManager(Dao dao) {
		super(dao);
	}

}