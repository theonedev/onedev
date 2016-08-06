package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestVerification;

@Editable(name="verification failed", icon="fa fa-thumbs-o-down")
public class PullRequestVerificationFailed extends PullRequestStatusChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationFailed(PullRequestVerification verification) {
		super(verification.getRequest(), verification.getUser(), 
				"configuration: " + verification.getConfiguration() + "\n"
				+ "message: " + verification.getMessage());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

}
