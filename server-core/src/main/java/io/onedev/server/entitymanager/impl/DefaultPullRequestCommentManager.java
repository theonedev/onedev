package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCommentDeleted;
import io.onedev.server.event.project.pullrequest.PullRequestCommentEdited;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

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
	public void delete(PullRequestComment comment) {
		super.delete(comment);
		comment.getRequest().setCommentCount(comment.getRequest().getCommentCount()-1);
		listenerRegistry.post(new PullRequestCommentDeleted(comment));
	}

	@Transactional
	@Override
	public void create(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		Preconditions.checkState(comment.isNew());
		dao.persist(comment);
		comment.getRequest().setCommentCount(comment.getRequest().getCommentCount()+1);
		listenerRegistry.post(new PullRequestCommentCreated(comment, notifiedEmailAddresses));
	}

	@Transactional
	@Override
	public void update(PullRequestComment comment) {
		Preconditions.checkState(!comment.isNew());
		dao.persist(comment);
		listenerRegistry.post(new PullRequestCommentEdited(comment));
	}
	
}
