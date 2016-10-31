package com.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.core.entity.CodeCommentStatusChange;
import com.gitplex.core.manager.CodeCommentStatusChangeManager;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;

@Singleton
public class DefaultCodeCommentStatusChangeManager extends AbstractEntityManager<CodeCommentStatusChange> 
		implements CodeCommentStatusChangeManager {

	@Inject
	public DefaultCodeCommentStatusChangeManager(Dao dao) {
		super(dao);
	}

}
