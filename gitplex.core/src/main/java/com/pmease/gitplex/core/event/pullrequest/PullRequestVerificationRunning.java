package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestVerification;

@Editable(name="ran verification", icon="fa fa-cog")
public class PullRequestVerificationRunning extends PullRequestChangeEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationRunning(PullRequestVerification verification) {
		super(verification.getRequest());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

	@Override
	public Account getUser() {
		return verification.getUser();
	}

	@Override
	public Date getDate() {
		return verification.getDate();
	}

}
