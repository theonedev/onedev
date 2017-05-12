package com.gitplex.server.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.codecomment.CodeCommentActivityEvent;
import com.gitplex.server.event.codecomment.CodeCommentCreated;
import com.gitplex.server.event.codecomment.CodeCommentEvent;
import com.gitplex.server.event.codecomment.CodeCommentResolved;
import com.gitplex.server.event.codecomment.CodeCommentUnresolved;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.google.common.base.Preconditions;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityManager<CodeComment> implements CodeCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	private final CodeCommentStatusChangeManager codeCommentStatusChangeManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final NotificationManager notificationManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry, 
			CodeCommentStatusChangeManager codeCommentStatusChangeManager,  
			PullRequestManager pullRequestManager, NotificationManager notificationManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.codeCommentStatusChangeManager = codeCommentStatusChangeManager;
		this.pullRequestManager = pullRequestManager;
		this.notificationManager = notificationManager;
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(Depot depot, ObjectId commitId, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.add(Restrictions.eq("commentPos.commit", commitId.name()));
		if (path != null)
			criteria.add(Restrictions.eq("commentPos.path", path));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(Depot depot, ObjectId... commitIds) {
		Preconditions.checkArgument(commitIds.length > 0);
		
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		List<Criterion> criterions = new ArrayList<>();
		for (ObjectId commitId: commitIds) {
			criterions.add(Restrictions.eq("commentPos.commit", commitId.name()));
		}
		criteria.add(Restrictions.or(criterions.toArray(new Criterion[criterions.size()])));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void save(CodeComment comment) {
		CodeCommentCreated event;
		if (comment.isNew()) {
			event = new CodeCommentCreated(comment);
		} else {
			event = null;
		}
		dao.persist(comment);
		if (event != null) {
			listenerRegistry.post(event);
		}
	}

	@Transactional
	@Override
	public void delete(CodeComment comment) {
		dao.remove(comment);
	}

	@Sessional
	@Override
	public CodeComment find(String uuid) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("uuid", uuid));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<CodeComment> findAllAfter(Depot depot, String commentUUID) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.addOrder(Order.asc("id"));
		if (commentUUID != null) {
			CodeComment comment = find(commentUUID);
			if (comment != null) {
				criteria.add(Restrictions.gt("id", comment.getId()));
			}
		}
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void changeStatus(CodeCommentStatusChange statusChange) {
		statusChange.getComment().setResolved(statusChange.isResolved());
		
		codeCommentStatusChangeManager.save(statusChange);
		
		CodeCommentEvent event;
		if (statusChange.isResolved()) {
			event = new CodeCommentResolved(statusChange);
		} else {
			event = new CodeCommentUnresolved(statusChange);
		}
		listenerRegistry.post(event);
		statusChange.getComment().setLastEvent(event);
		save(statusChange.getComment());
	}

	@Transactional
	@Listen
	public void on(CodeCommentActivityEvent event) {
		CodeCommentActivity activity = event.getActivity();
		
		for (CodeCommentRelation relation: activity.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			PullRequestCodeCommentActivityEvent pullRequestCodeCommentActivityEvent = event.getPullRequestCodeCommentActivityEvent(request);
			listenerRegistry.post(pullRequestCodeCommentActivityEvent);
			request.setLastEvent(pullRequestCodeCommentActivityEvent);
			
			pullRequestManager.save(request);
		}
		
		if (activity.getComment().getRelations().isEmpty())
			notificationManager.sendNotifications(event);
	}

}
