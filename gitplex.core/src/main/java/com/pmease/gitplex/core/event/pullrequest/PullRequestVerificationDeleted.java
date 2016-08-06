package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestVerification;

@Editable(name="deleted verification", icon="fa fa-times")
public class PullRequestVerificationDeleted extends PullRequestStatusChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationDeleted(PullRequestVerification verification, Account user) {
		super(verification.getRequest(), user, "configuration: " + verification.getConfiguration());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

}
