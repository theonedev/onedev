package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.manager.CodeCommentManager;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityDao<CodeComment> 
		implements CodeCommentManager {

	@Inject
	public DefaultCodeCommentManager(Dao dao) {
		super(dao);
	}

}
