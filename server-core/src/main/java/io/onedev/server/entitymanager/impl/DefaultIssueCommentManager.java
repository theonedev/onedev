package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueCommentEdited;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.support.issue.changedata.IssueCommentRemoveData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

@Singleton
public class DefaultIssueCommentManager extends BaseEntityManager<IssueComment> implements IssueCommentManager {
	
	private final IssueChangeManager changeManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueCommentManager(Dao dao, IssueChangeManager changeManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.changeManager = changeManager;
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
		
		IssueChange change = new IssueChange();
		change.setIssue(comment.getIssue());
		change.setUser(SecurityUtils.getUser());
		change.setData(new IssueCommentRemoveData());
		changeManager.create(change, null);
	}

	public void create(IssueComment comment) {
		create(comment, new ArrayList<>());
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
