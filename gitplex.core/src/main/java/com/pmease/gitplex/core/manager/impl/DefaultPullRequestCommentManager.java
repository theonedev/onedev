package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.extensionpoint.PullRequestListeners;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;

@Singleton
public class DefaultPullRequestCommentManager implements PullRequestCommentManager {

	private final Dao dao;
	
	private final PullRequestListeners pullRequestListeners;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao, PullRequestListeners pullRequestListeners) {
		this.dao = dao;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(PullRequestComment comment) {
		dao.persist(comment);

		final Long requestId = comment.getRequest().getId();
		
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				pullRequestListeners.asyncCall(requestId, new PullRequestListeners.Callback() {
					
					@Override
					protected void call(PullRequestListener listener, PullRequest request) {
						listener.onCommented(request);
					}
					
				});
			}
			
		});
	}

}
