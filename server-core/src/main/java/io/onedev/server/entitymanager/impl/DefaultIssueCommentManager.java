package io.onedev.server.entitymanager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;

import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssueCommented;
import io.onedev.server.model.IssueComment;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueCommentManager extends BaseEntityManager<IssueComment>
		implements IssueCommentManager {

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
	public void save(IssueComment comment) {
		save(comment, Lists.newArrayList());
	}

	@Transactional
	@Override
	public void delete(IssueComment comment) {
		super.delete(comment);
		comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()-1);
	}

	@Transactional
	@Override
	public void save(IssueComment comment, Collection<String> notifiedEmailAddresses) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (isNew) {
			comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()+1);
			
			Long commentId = comment.getId();
			sessionManager.runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					listenerRegistry.post(new IssueCommented(load(commentId), notifiedEmailAddresses));
				}
				
			});
		}		
	}

}
