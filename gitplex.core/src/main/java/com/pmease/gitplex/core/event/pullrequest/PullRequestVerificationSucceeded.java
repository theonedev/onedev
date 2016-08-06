package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestVerification;

@Editable(name="verification succeeded", icon="fa fa-thumbs-o-up")
public class PullRequestVerificationSucceeded extends PullRequestStatusChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationSucceeded(PullRequestVerification verification) {
		super(verification.getRequest(), verification.getUser(), 
				"configuration: " + verification.getConfiguration() + "\n"
				+ "message: " + verification.getMessage());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

}
