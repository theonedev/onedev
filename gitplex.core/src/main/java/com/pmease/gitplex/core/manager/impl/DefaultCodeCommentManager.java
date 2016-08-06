package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.support.LastEvent;
import com.pmease.gitplex.core.event.codecomment.CodeCommentCreated;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.event.codecomment.CodeCommentResolved;
import com.pmease.gitplex.core.event.codecomment.CodeCommentUnresolved;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentStatusChangeManager;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityManager<CodeComment> implements CodeCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	private final CodeCommentStatusChangeManager codeCommentActivityManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry, 
			CodeCommentStatusChangeManager codeCommentActivityManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.codeCommentActivityManager = codeCommentActivityManager;
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
			listenerRegistry.notify(event);
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
		save(statusChange.getComment());
		
		codeCommentActivityManager.save(statusChange);
		
		if (statusChange.isResolved())
			listenerRegistry.notify(new CodeCommentResolved(statusChange.getComment(), statusChange.getUser()));
		else
			listenerRegistry.notify(new CodeCommentUnresolved(statusChange.getComment(), statusChange.getUser()));
	}

	@Transactional
	@Listen
	public void on(CodeCommentReplied event) {
		CodeCommentReply reply = event.getReply();
		CodeComment comment = reply.getComment();
		LastEvent lastEvent = new LastEvent();
		lastEvent.setDate(reply.getDate());
		lastEvent.setDescription(EditableUtils.getName(event.getClass()));
		lastEvent.setUser(reply.getUser());
		comment.setLastEvent(lastEvent);
		save(comment);
	}

	@Transactional
	@Listen
	public void on(CodeCommentResolved event) {
		CodeComment comment = event.getComment();
		LastEvent lastEvent = new LastEvent();
		lastEvent.setDate(new Date());
		lastEvent.setDescription(EditableUtils.getName(event.getClass()));
		lastEvent.setUser(event.getUser());
		comment.setLastEvent(lastEvent);
		save(comment);
	}

	@Transactional
	@Listen
	public void on(CodeCommentUnresolved event) {
		CodeComment comment = event.getComment();
		LastEvent lastEvent = new LastEvent();
		lastEvent.setDate(new Date());
		lastEvent.setDescription(EditableUtils.getName(event.getClass()));
		lastEvent.setUser(event.getUser());
		comment.setLastEvent(lastEvent);
		save(comment);
	}

}
