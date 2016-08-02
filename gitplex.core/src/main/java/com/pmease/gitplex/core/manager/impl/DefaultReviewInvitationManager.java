package com.pmease.gitplex.core.manager.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.event.PullRequestListener;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;

@Singleton
public class DefaultReviewInvitationManager extends AbstractEntityManager<ReviewInvitation> implements ReviewInvitationManager {

	private final Provider<Set<PullRequestListener>> listenersProvider;
	
	@Inject
	public DefaultReviewInvitationManager(Dao dao, Provider<Set<PullRequestListener>> listenersProvider) {
		super(dao);
		
		this.listenersProvider = listenersProvider;
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
		dao.persist(invitation);
		
		for (PullRequestListener listener: listenersProvider.get())
			listener.onInvitingReview(invitation);
	}

}
