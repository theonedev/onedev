package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.codecomment.CodeCommentCreated;
import com.pmease.gitplex.core.event.codecomment.CodeCommentEvent;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.event.codecomment.CodeCommentResolved;
import com.pmease.gitplex.core.event.codecomment.CodeCommentUnresolved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentReplied;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentResolved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentUnresolved;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentStatusChangeManager;
import com.pmease.gitplex.core.manager.PullRequestManager;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityManager<CodeComment> implements CodeCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	private final CodeCommentStatusChangeManager codeCommentStatusChangeManager;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry, 
			CodeCommentStatusChangeManager codeCommentStatusChangeManager, PullRequestManager pullRequestManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.codeCommentStatusChangeManager = codeCommentStatusChangeManager;
		this.pullRequestManager = pullRequestManager;
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
	public void save(CodeComment comment, PullRequest request) {
		CodeCommentCreated event;
		if (comment.isNew()) {
			event = new CodeCommentCreated(comment, request);
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
	public void changeStatus(CodeCommentStatusChange statusChange, PullRequest request) {
		statusChange.getComment().setResolved(statusChange.isResolved());
		
		codeCommentStatusChangeManager.save(statusChange);
		
		CodeCommentEvent event;
		if (statusChange.isResolved()) {
			event = new CodeCommentResolved(statusChange, request);
		} else {
			event = new CodeCommentUnresolved(statusChange, request);
		}
		listenerRegistry.post(event);
		statusChange.getComment().setLastEvent(event);
		save(statusChange.getComment());
	}

	@Transactional
	@Listen
	public void on(CodeCommentReplied event) {
		CodeCommentReply reply = event.getReply();
		
		for (CodeCommentRelation relation: reply.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			PullRequestCodeCommentReplied pullRequestCodeCommentReplied = 
					new PullRequestCodeCommentReplied(request, reply);
			listenerRegistry.post(pullRequestCodeCommentReplied);
			request.setLastEvent(pullRequestCodeCommentReplied);
			
			pullRequestManager.save(request);
		}
		
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentResolved event) {
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			PullRequestCodeCommentResolved pullRequestCodeCommentResolved =
					new PullRequestCodeCommentResolved(request, event.getStatusChange());
			listenerRegistry.post(pullRequestCodeCommentResolved);
			request.setLastEvent(pullRequestCodeCommentResolved);
			
			pullRequestManager.save(request);
		}
	}

	@Transactional
	@Listen
	public void on(CodeCommentUnresolved event) {
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			PullRequestCodeCommentUnresolved pullRequestCodeCommentUnresolved =
					new PullRequestCodeCommentUnresolved(request, event.getStatusChange());
			listenerRegistry.post(pullRequestCodeCommentUnresolved);
			request.setLastEvent(pullRequestCodeCommentUnresolved);
			
			pullRequestManager.save(request);
		}
	}

}
