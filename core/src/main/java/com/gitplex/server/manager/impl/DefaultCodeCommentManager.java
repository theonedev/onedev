package com.gitplex.server.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentResolved;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentUnresolved;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.PullRequest;
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
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry,
			CodeCommentStatusChangeManager codeCommentStatusChangeManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.codeCommentStatusChangeManager = codeCommentStatusChangeManager;
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(PullRequest request, ObjectId commitId, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
		criteria.add(Restrictions.eq("commentPos.commit", commitId.name()));
		if (path != null)
			criteria.add(Restrictions.eq("commentPos.path", path));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(PullRequest request, ObjectId... commitIds) {
		Preconditions.checkArgument(commitIds.length > 0);
		
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
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
		PullRequestCodeCommentCreated event;
		if (comment.isNew()) {
			event = new PullRequestCodeCommentCreated(comment);
			dao.persist(comment);
			listenerRegistry.post(event);
		} else {
			dao.persist(comment);
		}
	}

	@Transactional
	@Override
	public void changeStatus(CodeCommentStatusChange statusChange) {
		statusChange.getComment().setResolved(statusChange.isResolved());
		
		codeCommentStatusChangeManager.save(statusChange);
		
		PullRequestCodeCommentEvent event;
		if (statusChange.isResolved()) {
			event = new PullRequestCodeCommentResolved(statusChange);
		} else {
			event = new PullRequestCodeCommentUnresolved(statusChange);
		}
		listenerRegistry.post(event);
		statusChange.getComment().setLastEvent(event);
		save(statusChange.getComment());
	}

}
