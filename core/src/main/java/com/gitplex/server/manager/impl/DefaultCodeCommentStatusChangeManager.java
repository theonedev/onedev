package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultCodeCommentStatusChangeManager extends AbstractEntityManager<CodeCommentStatusChange> 
		implements CodeCommentStatusChangeManager {

	@Inject
	public DefaultCodeCommentStatusChangeManager(Dao dao) {
		super(dao);
	}

}
