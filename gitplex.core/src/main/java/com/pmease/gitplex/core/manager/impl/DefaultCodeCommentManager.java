package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.TransactionInterceptor;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.CodeCommentEvent;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityManager<CodeComment> 
		implements CodeCommentManager, CodeCommentListener {

	private final Provider<Set<CodeCommentListener>> listenersProvider;
	
	private final CodeCommentReplyManager codeCommentReplyManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, Provider<Set<CodeCommentListener>> listenersProvider, 
			CodeCommentReplyManager codeCommentReplyManager) {
		super(dao);
		this.listenersProvider = listenersProvider;
		this.codeCommentReplyManager = codeCommentReplyManager;
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
		boolean isNew;
		if (comment.isNew()) {
			comment.setLastEvent(CodeCommentEvent.CREATED);
			comment.setLastEventDate(comment.getCreateDate());
			comment.setLastEventUser(comment.getUser());
			isNew = true;
		} else {
			isNew = false;
		}
		dao.persist(comment);
		if (TransactionInterceptor.isInitiating() && isNew) {
			for (CodeCommentListener listener: listenersProvider.get())
				listener.onComment(comment);
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
	public void toggleResolve(CodeComment comment, CodeCommentReply reply) {
		save(comment);
		if (reply != null) 
			codeCommentReplyManager.save(reply);
		
		if (TransactionInterceptor.isInitiating()) {
			for (CodeCommentListener listener: listenersProvider.get()) {
				listener.onToggleResolve(comment, reply.getUser());
			}
		}
	}

	@Transactional
	@Override
	public void toggleResolve(CodeComment comment, Account user) {
		save(comment);
		if (TransactionInterceptor.isInitiating()) {
			for (CodeCommentListener listener: listenersProvider.get()) {
				listener.onToggleResolve(comment, user);
			}
		}
	}
	
	@Override
	public void onComment(CodeComment comment) {
	}

	@Transactional
	@Override
	public void onReplyComment(CodeCommentReply reply) {
		reply.getComment().setLastEvent(CodeCommentEvent.REPLIED);
		reply.getComment().setLastEventDate(reply.getDate());
		reply.getComment().setLastEventUser(reply.getUser());
		save(reply.getComment());
	}

	@Override
	public void onToggleResolve(CodeComment comment, Account user) {
		if (comment.isResolved())
			comment.setLastEvent(CodeCommentEvent.RESOLVED);
		else
			comment.setLastEvent(CodeCommentEvent.UNRESOLVED);
		comment.setLastEventDate(new Date());
		comment.setLastEventUser(user);
		save(comment);
	}

}
