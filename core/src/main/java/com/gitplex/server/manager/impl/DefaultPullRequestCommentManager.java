package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.entity.PullRequestComment;
import com.gitplex.server.entity.support.LastEvent;
import com.gitplex.server.event.pullrequest.PullRequestCommentCreated;
import com.gitplex.server.manager.PullRequestCommentManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.util.editable.EditableUtils;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractEntityManager<PullRequestComment> 
		implements PullRequestCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, PullRequestManager pullRequestManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);

		this.listenerRegistry = listenerRegistry;
		this.pullRequestManager = pullRequestManager;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		save(comment, true);
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment, boolean notifyListeners) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (notifyListeners && isNew) {
			PullRequestCommentCreated event = new PullRequestCommentCreated(comment);
			listenerRegistry.post(event);
			
			LastEvent lastEvent = new LastEvent();
			lastEvent.setDate(event.getDate());
			lastEvent.setType(EditableUtils.getName(event.getClass()));
			lastEvent.setUser(event.getUser());
			comment.getRequest().setLastEvent(lastEvent);
			pullRequestManager.save(event.getRequest());
		}
	}

}
