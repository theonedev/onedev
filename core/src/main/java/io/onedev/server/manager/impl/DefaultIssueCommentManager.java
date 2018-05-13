package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueCommentManager extends AbstractEntityManager<IssueComment>
		implements IssueCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueCommentManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Override
	public void save(IssueComment comment) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (isNew) {
			IssueCommented event = new IssueCommented(comment);
			listenerRegistry.post(event);
		}		
	}

}
