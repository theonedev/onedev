package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.ListenerRegistry;
import com.turbodev.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.turbodev.server.manager.PullRequestStatusChangeManager;
import com.turbodev.server.model.PullRequestStatusChange;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;

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
	public void save(PullRequestStatusChange statusChange, Object statusData) {
		dao.persist(statusChange);
		listenerRegistry.post(new PullRequestStatusChangeEvent(statusChange, statusData));
	}

	@Override
	public void save(PullRequestStatusChange statusChange) {
		save(statusChange, null);
	}
	
}
