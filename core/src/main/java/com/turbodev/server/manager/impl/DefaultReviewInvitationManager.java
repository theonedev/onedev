package com.turbodev.server.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.manager.PullRequestStatusChangeManager;
import com.turbodev.server.manager.ReviewInvitationManager;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.PullRequestStatusChange;
import com.turbodev.server.model.ReviewInvitation;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.security.SecurityUtils;

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
		
		User reviewer = invitation.getUser();
		if (request.getQualityCheckStatus().getAwaitingReviewers().contains(reviewer) 
				|| request.getQualityCheckStatus().getEffectiveReviews().containsKey(reviewer)) {
			return false;
		} else {
			save(invitation);
			
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setRequest(request);
			statusChange.setType(PullRequestStatusChange.Type.REMOVED_REVIEWER);
			statusChange.setUser(SecurityUtils.getUser());
			statusChange.setNote("User '" + reviewer.getDisplayName() + "' is removed from reviewer list");
			pullRequestStatusChangeManager.save(statusChange, invitation);
			
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
		statusChange.setUser(SecurityUtils.getUser());
		pullRequestStatusChangeManager.save(statusChange, invitation);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
	}
	
}
