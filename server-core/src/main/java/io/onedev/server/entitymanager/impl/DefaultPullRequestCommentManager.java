package io.onedev.server.entitymanager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;

import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestCommented;
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
		save(comment, Lists.newArrayList());
	}

	@Transactional
	@Override
	public void delete(PullRequestComment comment) {
		super.delete(comment);
		PullRequest request = comment.getRequest();
		request.setCommentCount(request.getCommentCount()-1);
	}

	@Override
	public void save(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (isNew) {
			PullRequestCommented event = new PullRequestCommented(comment, notifiedEmailAddresses);
			listenerRegistry.post(event);
			
			PullRequest request = comment.getRequest();
			request.setCommentCount(request.getCommentCount()+1);
		}
	}

}
