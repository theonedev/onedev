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
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentReplyManager;
import com.pmease.gitplex.core.model.CommentReply;
import com.pmease.gitplex.core.model.PullRequest;

@Singleton
public class DefaultCommentReplyManager implements CommentReplyManager {

	private final Dao dao;

	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultCommentReplyManager(Dao dao, Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(CommentReply reply) {
		dao.persist(reply);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onCommentReplied(reply);
	}

	@Sessional
	@Override
	public Collection<CommentReply> findBy(PullRequest request) {
		EntityCriteria<CommentReply> criteria = EntityCriteria.of(CommentReply.class);
		criteria.createCriteria("comment").add(Restrictions.eq("request", request));
		return dao.query(criteria);
	}

}
