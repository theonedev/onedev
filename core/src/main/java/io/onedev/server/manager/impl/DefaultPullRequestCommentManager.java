package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestCommented;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.editable.EditableUtils;

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
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (isNew) {
			PullRequestCommented event = new PullRequestCommented(comment);
			listenerRegistry.post(event);
			
			LastActivity lastEvent = new LastActivity();
			lastEvent.setDate(event.getDate());
			lastEvent.setAction(EditableUtils.getDisplayName(event.getClass()));
			lastEvent.setUser(event.getUser());
			comment.getRequest().setLastActivity(lastEvent);
			pullRequestManager.save(event.getRequest());
		}
	}

}
