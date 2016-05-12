package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.CodeCommentManager;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityDao<CodeComment> 
		implements CodeCommentManager {

	@Inject
	public DefaultCodeCommentManager(Dao dao) {
		super(dao);
	}

	@Override
	public Collection<CodeComment> query(Depot depot, String commit, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.add(Restrictions.eq("commit", commit));
		if (path != null)
			criteria.add(Restrictions.eq("path", path));
		return query(criteria);
	}

}
