package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.extensionpoint.PullRequestListeners;
import com.pmease.gitplex.core.manager.PullRequestCommentReplyManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestCommentReply;

@Singleton
public class DefaultPullRequestCommentReplyManager implements PullRequestCommentReplyManager {

	private final Dao dao;
	
	private final PullRequestListeners pullRequestListeners;
	
	@Inject
	public DefaultPullRequestCommentReplyManager(Dao dao, PullRequestListeners pullRequestListeners) {
		this.dao = dao;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(PullRequestCommentReply reply) {
		dao.persist(reply);

		final Long requestId = reply.getComment().getRequest().getId();
		
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

	@Sessional
	@Override
	public Collection<PullRequestCommentReply> findBy(PullRequest request) {
		EntityCriteria<PullRequestCommentReply> criteria = EntityCriteria.of(PullRequestCommentReply.class);
		criteria.createCriteria("comment").add(Restrictions.eq("request", request));
		return dao.query(criteria);
	}

}
