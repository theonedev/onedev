package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestVerification;

@Editable(name="completed verification (failed)", icon="fa fa-thumbs-o-down")
public class PullRequestVerificationFailed extends PullRequestStatusChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationFailed(PullRequestVerification verification) {
		super(verification.getRequest(), verification.getUser(), new Date(),
				"configuration: " + verification.getConfiguration() + "\n"
				+ "message: " + verification.getMessage());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

}
