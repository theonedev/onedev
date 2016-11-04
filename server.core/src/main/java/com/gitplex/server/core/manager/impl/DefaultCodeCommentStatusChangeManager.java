package com.gitplex.server.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.server.core.entity.CodeCommentStatusChange;
import com.gitplex.server.core.manager.CodeCommentStatusChangeManager;

@Singleton
public class DefaultCodeCommentStatusChangeManager extends AbstractEntityManager<CodeCommentStatusChange> 
		implements CodeCommentStatusChangeManager {

	@Inject
	public DefaultCodeCommentStatusChangeManager(Dao dao) {
		super(dao);
	}

}
