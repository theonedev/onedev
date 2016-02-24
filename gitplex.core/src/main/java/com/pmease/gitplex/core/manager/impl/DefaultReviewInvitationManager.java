package com.pmease.gitplex.core.manager.impl;

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
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;

@Singleton
public class DefaultReviewInvitationManager extends DefaultDao implements ReviewInvitationManager {

	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultReviewInvitationManager(Provider<Session> sessionProvider, 
			Set<PullRequestListener> pullRequestListeners) {
		super(sessionProvider);
		
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public ReviewInvitation find(User reviewer, PullRequest request) {
		return find(EntityCriteria.of(ReviewInvitation.class)
				.add(Restrictions.eq("reviewer", reviewer))
				.add(Restrictions.eq("request", request)));
	}

	@Transactional
	@Override
	public void save(ReviewInvitation invitation) {
		persist(invitation);
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onInvitingReview(invitation);
	}

}
