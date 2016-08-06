package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;

public class InvitingPullRequestReview extends PullRequestNotificationEvent {

	private final PullRequestReviewInvitation invitation;
	
	public InvitingPullRequestReview(PullRequestReviewInvitation invitation) {
		super(invitation.getRequest());
		this.invitation = invitation;
	}

	public PullRequestReviewInvitation getInvitation() {
		return invitation;
	}

}
