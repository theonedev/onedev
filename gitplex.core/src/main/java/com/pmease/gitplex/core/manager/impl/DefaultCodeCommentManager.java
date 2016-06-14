package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
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
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.manager.CodeCommentManager;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityDao<CodeComment> implements CodeCommentManager {

	private final Provider<Set<CodeCommentListener>> listenersProvider;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, Provider<Set<CodeCommentListener>> listenersProvider) {
		super(dao);
		this.listenersProvider = listenersProvider;
	}

	@Sessional
	@Override
	public Collection<CodeComment> query(Depot depot, ObjectId commitId, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.add(Restrictions.eq("commit", commitId.name()));
		if (path != null)
			criteria.add(Restrictions.eq("path", path));
		return query(criteria);
	}

	@Sessional
	@Override
	public Collection<CodeComment> query(Depot depot, ObjectId... commitIds) {
		Preconditions.checkArgument(commitIds.length > 0);
		
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		List<Criterion> criterions = new ArrayList<>();
		for (ObjectId commitId: commitIds) {
			criterions.add(Restrictions.eq("commit", commitId.name()));
		}
		criteria.add(Restrictions.or(criterions.toArray(new Criterion[criterions.size()])));
		return query(criteria);
	}

	@Transactional
	@Override
	public void save(CodeComment comment) {
		for (CodeCommentListener listener: listenersProvider.get())
			listener.onSaveComment(comment);
		dao.persist(comment);
	}

	@Transactional
	@Override
	public void delete(CodeComment comment) {
		for (CodeCommentListener listener: listenersProvider.get())
			listener.onDeleteComment(comment);
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
	public List<CodeComment> queryAfter(Depot depot, String commentUUID) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.addOrder(Order.asc("id"));
		if (commentUUID != null) {
			CodeComment comment = find(commentUUID);
			if (comment != null) {
				criteria.add(Restrictions.gt("id", comment.getId()));
			}
		}
		return query(criteria);
	}

}
