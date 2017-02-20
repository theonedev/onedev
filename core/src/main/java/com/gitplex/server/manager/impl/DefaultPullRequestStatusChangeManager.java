package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestStatusChangeManager extends AbstractEntityManager<PullRequestStatusChange> 
		implements PullRequestStatusChangeManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestStatusChangeManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Override
	public void save(PullRequestStatusChange statusChange) {
		dao.persist(statusChange);
		listenerRegistry.post(new PullRequestStatusChangeEvent(statusChange));
	}
	
}
