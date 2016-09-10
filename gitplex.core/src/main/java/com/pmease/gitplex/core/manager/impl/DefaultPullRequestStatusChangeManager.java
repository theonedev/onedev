package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.pmease.gitplex.core.manager.PullRequestStatusChangeManager;

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
