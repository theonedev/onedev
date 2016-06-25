package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractEntityDao<PullRequestComment> 
		implements PullRequestCommentManager {

	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, Set<PullRequestListener> pullRequestListeners) {
		super(dao);
		
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		persist(comment);
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onCommentRequest(comment);
	}

	@Transactional
	@Override
	public void delete(PullRequestComment comment) {
		remove(comment);
	}

}
