package com.gitplex.server.event;

import com.gitplex.server.model.ReviewInvitation;

public class ReviewInvitationChanged {

	private final ReviewInvitation invitation;
	
	public ReviewInvitationChanged(ReviewInvitation invitation) {
		this.invitation = invitation;
	}

	public ReviewInvitation getInvitation() {
		return invitation;
	}

}
