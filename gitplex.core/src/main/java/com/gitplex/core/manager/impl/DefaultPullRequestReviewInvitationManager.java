package com.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestReviewInvitation;
import com.gitplex.core.event.pullrequest.PullRequestReviewInvitationChanged;
import com.gitplex.core.manager.PullRequestReviewInvitationManager;
import com.gitplex.core.manager.PullRequestReviewManager;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.commons.loader.ListenerRegistry;

@Singleton
public class DefaultPullRequestReviewInvitationManager extends AbstractEntityManager<PullRequestReviewInvitation> 
		implements PullRequestReviewInvitationManager {

	private final PullRequestReviewManager reviewManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestReviewInvitationManager(Dao dao, PullRequestReviewManager reviewManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);
		
		this.reviewManager = reviewManager;
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
		
		listenerRegistry.post(new PullRequestReviewInvitationChanged(invitation));
	}

	@Transactional
	@Override
	public void update(Collection<PullRequestReviewInvitation> invitations, Date since) {
		for (PullRequestReviewInvitation invitation: invitations) {
			if (invitation.getDate().getTime()>=since.getTime()) {
				save(invitation);
				if (invitation.getStatus() == PullRequestReviewInvitation.Status.EXCLUDED)
					reviewManager.deleteAll(invitation.getUser(), invitation.getRequest());
			}
		}
	}
}
