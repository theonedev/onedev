package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.server.manager.PullRequestWatchManager;
import com.turbodev.server.model.PullRequestWatch;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestWatchManager extends AbstractEntityManager<PullRequestWatch> 
		implements PullRequestWatchManager {

	@Inject
	public DefaultPullRequestWatchManager(Dao dao) {
		super(dao);
	}
}
