package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestCommentCreated;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.LastEvent;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.editable.EditableUtils;

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
