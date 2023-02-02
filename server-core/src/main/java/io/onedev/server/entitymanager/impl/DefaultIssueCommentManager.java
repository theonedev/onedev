package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueCommentDeleted;
import io.onedev.server.event.project.issue.IssueCommentUpdated;
import io.onedev.server.model.IssueComment;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class DefaultIssueCommentManager extends BaseEntityManager<IssueComment> implements IssueCommentManager {
	
	private final ListenerRegistry listenerRegistry;
	
	private final SessionManager sessionManager;
	
	@Inject
	public DefaultIssueCommentManager(Dao dao, ListenerRegistry listenerRegistry, SessionManager sessionManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.sessionManager = sessionManager;
	}

	@Transactional
	@Override
	public void update(IssueComment comment) {
		dao.persist(comment);
		listenerRegistry.post(new IssueCommentUpdated(comment));
	}

	@Transactional
	@Override
	public void delete(IssueComment comment) {
		dao.remove(comment);
		
		comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()-1);
		listenerRegistry.post(new IssueCommentDeleted(comment));
	}

	@Transactional
	@Override
	public void create(IssueComment comment, Collection<String> notifiedEmailAddresses) {
		dao.persist(comment);
		comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()+1);
		listenerRegistry.post(new IssueCommentCreated(comment, notifiedEmailAddresses));
	}

}
