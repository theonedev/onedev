package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestCommentManager;
import com.pmease.gitop.model.PullRequestComment;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractGenericDao<PullRequestComment> 
		implements PullRequestCommentManager {

	@Inject
	public DefaultPullRequestCommentManager(GeneralDao generalDao) {
		super(generalDao);
	}
}
