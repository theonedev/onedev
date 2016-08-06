package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCommented;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractEntityManager<PullRequestComment> 
		implements PullRequestCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);

		this.listenerRegistry = listenerRegistry;
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
			listenerRegistry.notify(new PullRequestCommented(comment));
		}
	}

}
