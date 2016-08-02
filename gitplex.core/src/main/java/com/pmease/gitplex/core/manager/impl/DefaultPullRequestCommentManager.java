package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.event.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractEntityManager<PullRequestComment> 
		implements PullRequestCommentManager {

	private final Provider<Set<PullRequestListener>> listenersProvider;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, Provider<Set<PullRequestListener>> listenersProvider) {
		super(dao);

		this.listenersProvider = listenersProvider;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		save(comment, true);
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment, boolean notifyListeners) {
		boolean isNew = comment.isNew();
		dao.persist(comment);
		if (notifyListeners && isNew) {
			for (PullRequestListener listener: listenersProvider.get())
				listener.onCommentRequest(comment);
		}
	}

}
