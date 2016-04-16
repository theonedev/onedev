package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentManager;

@Singleton
public class DefaultCommentManager extends AbstractEntityDao<Comment> implements CommentManager {

	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultCommentManager(Dao dao, Set<PullRequestListener> pullRequestListeners) {
		super(dao);
		
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(Comment comment, boolean notify) {
		persist(comment);
		
		if (notify) {
			for (PullRequestListener listener: pullRequestListeners)
				listener.onCommented(comment);
		}
	}

}
