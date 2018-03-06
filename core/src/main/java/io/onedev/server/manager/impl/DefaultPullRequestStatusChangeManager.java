package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestStatusChangeEvent;
import io.onedev.server.manager.PullRequestStatusChangeManager;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
