package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;

@Singleton
public class DefaultReviewInvitationManager extends AbstractEntityDao<ReviewInvitation> implements ReviewInvitationManager {

	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultReviewInvitationManager(Dao dao, 
			Set<PullRequestListener> pullRequestListeners) {
		super(dao);
		
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public ReviewInvitation find(Account reviewer, PullRequest request) {
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
