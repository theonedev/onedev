package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentReplyManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestCommentReply;

@Singleton
public class DefaultPullRequestCommentReplyManager implements PullRequestCommentReplyManager {

	private final Dao dao;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultPullRequestCommentReplyManager(Dao dao, Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(final PullRequestCommentReply reply) {
		dao.persist(reply);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onCommentReplied(reply);
	}

	@Sessional
	@Override
	public Collection<PullRequestCommentReply> findBy(PullRequest request) {
		EntityCriteria<PullRequestCommentReply> criteria = EntityCriteria.of(PullRequestCommentReply.class);
		criteria.createCriteria("comment").add(Restrictions.eq("request", request));
		return dao.query(criteria);
	}

}
