package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.DefaultDao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentReplyManager;

@Singleton
public class DefaultCommentReplyManager extends DefaultDao implements CommentReplyManager {

	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultCommentReplyManager(Provider<Session> sessionProvider, 
			Set<PullRequestListener> pullRequestListeners) {
		super(sessionProvider);
		
		this.pullRequestListeners = pullRequestListeners;
	}

	@Transactional
	@Override
	public void save(CommentReply reply) {
		persist(reply);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onCommentReplied(reply);
	}

	@Sessional
	@Override
	public Collection<CommentReply> findBy(PullRequest request) {
		EntityCriteria<CommentReply> criteria = EntityCriteria.of(CommentReply.class);
		criteria.createCriteria("comment").add(Restrictions.eq("request", request));
		return query(criteria);
	}

}
