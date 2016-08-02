package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.manager.CodeCommentStatusChangeManager;

@Singleton
public class DefaultCodeCommentStatusChangeManager extends AbstractEntityManager<CodeCommentStatusChange> 
		implements CodeCommentStatusChangeManager {

	@Inject
	public DefaultCodeCommentStatusChangeManager(Dao dao) {
		super(dao);
	}

}
