package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.PullRequestWatchManager;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestWatchManager extends AbstractEntityManager<PullRequestWatch> 
		implements PullRequestWatchManager {

	@Inject
	public DefaultPullRequestWatchManager(Dao dao) {
		super(dao);
	}
}
