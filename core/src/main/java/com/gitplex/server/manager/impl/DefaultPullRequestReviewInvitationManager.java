package com.gitplex.server.manager.impl;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.pullrequest.PullRequestReviewInvitationChanged;
import com.gitplex.server.manager.PullRequestReviewInvitationManager;
import com.gitplex.server.manager.PullRequestReviewManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestReviewInvitation;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

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
