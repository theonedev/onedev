package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequestReviewInvitation;

public class PullRequestReviewInvitationChanged extends PullRequestEvent {

	private final PullRequestReviewInvitation invitation;
	
	public PullRequestReviewInvitationChanged(PullRequestReviewInvitation invitation) {
		super(invitation.getRequest());
		this.invitation = invitation;
	}

	public PullRequestReviewInvitation getInvitation() {
		return invitation;
	}

}
