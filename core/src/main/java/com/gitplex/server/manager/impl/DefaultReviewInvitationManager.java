package com.gitplex.server.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.manager.ReviewInvitationManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.SecurityUtils;

@Singleton
public class DefaultReviewInvitationManager extends AbstractEntityManager<ReviewInvitation> 
		implements ReviewInvitationManager {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	@Inject
	public DefaultReviewInvitationManager(Dao dao, PullRequestManager pullRequestManager, 
			PullRequestStatusChangeManager pullRequestStatusChangeManger) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.pullRequestStatusChangeManager = pullRequestStatusChangeManger;
	}

	@Transactional
	@Override
	public boolean exclude(ReviewInvitation invitation) {
		invitation.setType(ReviewInvitation.Type.EXCLUDE);
		PullRequest request = invitation.getRequest();
		
		save(invitation);
		
		Account reviewer = invitation.getUser();
		if (request.getReviewStatus().getAwaitingReviewers().contains(reviewer) 
				|| request.getReviewStatus().getEffectiveReviews().containsKey(reviewer)) {
			return false;
		} else {
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setRequest(request);
			statusChange.setType(PullRequestStatusChange.Type.REMOVED_REVIEWER);
			statusChange.setUser(SecurityUtils.getAccount());
			statusChange.setNote("User '" + reviewer.getDisplayName() + "' is removed from reviewer list");
			pullRequestStatusChangeManager.save(statusChange);
			
			request.setLastEvent(statusChange);
			pullRequestManager.save(request);
			return true;
		}
	}

	@Transactional
	@Override
	public void invite(ReviewInvitation invitation) {
		save(invitation);
		
		PullRequest request = invitation.getRequest();
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setRequest(request);
		statusChange.setType(PullRequestStatusChange.Type.ADDED_REVIEWER);
		statusChange.setNote("User '" +  invitation.getUser().getDisplayName() + "' is added as a reviewer");
		statusChange.setUser(SecurityUtils.getAccount());
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
	}
	
}
