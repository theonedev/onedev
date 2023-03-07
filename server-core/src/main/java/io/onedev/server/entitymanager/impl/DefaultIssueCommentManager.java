package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueCommentDeleted;
import io.onedev.server.event.project.issue.IssueCommentEdited;
import io.onedev.server.model.IssueComment;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class DefaultIssueCommentManager extends BaseEntityManager<IssueComment> implements IssueCommentManager {
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueCommentManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void update(IssueComment comment) {
 		Preconditions.checkState(!comment.isNew());
		dao.persist(comment);
		listenerRegistry.post(new IssueCommentEdited(comment));
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
		Preconditions.checkState(comment.isNew());
		dao.persist(comment);
		comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()+1);
		listenerRegistry.post(new IssueCommentCreated(comment, notifiedEmailAddresses));
	}

}
