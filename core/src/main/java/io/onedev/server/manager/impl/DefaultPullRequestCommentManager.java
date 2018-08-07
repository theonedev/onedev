package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestCommentAdded;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
			PullRequestCommentAdded event = new PullRequestCommentAdded(comment);
			listenerRegistry.post(event);
			
			LastActivity lastEvent = new LastActivity();
			lastEvent.setDate(event.getDate());
			lastEvent.setDescription("added comment");
			lastEvent.setUser(event.getUser());
			
			PullRequest request = comment.getRequest();
			request.setCommentCount(request.getCommentCount()+1);
			request.setLastActivity(lastEvent);
			pullRequestManager.save(event.getRequest());
		}
	}

	@Transactional
	@Override
	public void delete(PullRequestComment comment) {
		super.delete(comment);
		PullRequest request = comment.getRequest();
		request.setCommentCount(request.getCommentCount()-1);
	}

}
