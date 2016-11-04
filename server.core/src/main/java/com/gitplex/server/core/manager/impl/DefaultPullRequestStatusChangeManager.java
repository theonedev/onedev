package com.gitplex.server.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.loader.ListenerRegistry;
import com.gitplex.server.core.entity.PullRequestStatusChange;
import com.gitplex.server.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.core.manager.PullRequestStatusChangeManager;

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
