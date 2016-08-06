package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequestVerification;

public class PullRequestVerificationRunning extends PullRequestChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationRunning(PullRequestVerification verification) {
		super(verification.getRequest());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

}
