package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.component.PullRequestEvent;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;

@Singleton
public class DefaultPullRequestCommentManager extends AbstractEntityManager<PullRequestComment> 
		implements PullRequestCommentManager {

	private final PullRequestManager pullRequestManager;
	
	private final Provider<Set<PullRequestListener>> listenersProvider;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, PullRequestManager pullRequestManager, 
			Provider<Set<PullRequestListener>> listenersProvider) {
		super(dao);

		this.pullRequestManager = pullRequestManager;
		this.listenersProvider = listenersProvider;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		dao.persist(comment);
		
		PullRequest request = comment.getRequest();
		request.setLastEvent(PullRequestEvent.COMMENTED);
		request.setLastEventUser(comment.getUser());
		request.setLastEventDate(new Date());
		pullRequestManager.save(request);
		
		for (PullRequestListener listener: listenersProvider.get())
			listener.onCommentRequest(comment);
	}

	@Transactional
	@Override
	public void delete(PullRequestComment comment) {
		dao.remove(comment);
	}

}
