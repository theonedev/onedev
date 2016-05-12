package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;

@Singleton
public class DefaultCodeCommentReplyManager extends AbstractEntityDao<CodeCommentReply> 
		implements CodeCommentReplyManager {

	@Inject
	public DefaultCodeCommentReplyManager(Dao dao) {
		super(dao);
	}

}
