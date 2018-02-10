package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.ListenerRegistry;
import com.turbodev.server.event.pullrequest.PullRequestCommentCreated;
import com.turbodev.server.manager.PullRequestCommentManager;
import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.model.PullRequestComment;
import com.turbodev.server.model.support.LastEvent;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.util.editable.EditableUtils;

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
