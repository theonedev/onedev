package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.event.pullrequest.InvitingPullRequestReview;
import com.pmease.gitplex.core.manager.PullRequestReviewInvitationManager;

@Singleton
public class DefaultPullRequestReviewInvitationManager extends AbstractEntityManager<PullRequestReviewInvitation> 
		implements PullRequestReviewInvitationManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestReviewInvitationManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		
		this.listenerRegistry = listenerRegistry;
	}

	@Sessional
	@Override
	public PullRequestReviewInvitation find(Account reviewer, PullRequest request) {
		return find(EntityCriteria.of(PullRequestReviewInvitation.class)
				.add(Restrictions.eq("reviewer", reviewer))
				.add(Restrictions.eq("request", request)));
	}

	@Transactional
	@Override
	public void save(PullRequestReviewInvitation invitation) {
		dao.persist(invitation);
		
		listenerRegistry.notify(new InvitingPullRequestReview(invitation));
	}

}
