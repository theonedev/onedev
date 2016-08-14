package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestVerification;

@Editable(name="completed verification (successful)", icon="fa fa-thumbs-o-up")
public class PullRequestVerificationSucceeded extends PullRequestStatusChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationSucceeded(PullRequestVerification verification) {
		super(verification.getRequest(), verification.getUser(), new Date(),
				"configuration: " + verification.getConfiguration() + "\n"
				+ "message: " + verification.getMessage());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

}
