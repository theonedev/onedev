package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.event.pullrequest.PullRequestCommentCreated;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestCommentManager extends BaseEntityManager<PullRequestComment> 
		implements PullRequestCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (isNew) {
			PullRequestCommentCreated event = new PullRequestCommentCreated(comment);
			listenerRegistry.post(event);
			
			PullRequest request = comment.getRequest();
			request.setCommentCount(request.getCommentCount()+1);
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
