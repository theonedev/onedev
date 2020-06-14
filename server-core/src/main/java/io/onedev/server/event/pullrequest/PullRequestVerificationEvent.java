package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequestVerification;

public class PullRequestVerificationEvent extends PullRequestEvent {

	private final PullRequestVerification verification;
	
	public PullRequestVerificationEvent(PullRequestVerification verification) {
		super(null, new Date(), verification.getRequest());
		this.verification = verification;
	}

	public PullRequestVerification getVerification() {
		return verification;
	}

	@Override
	public String getActivity(boolean withEntity) {
		Build build = verification.getBuild();
		String activity = build.getJobName() + " ";
		if (build.getVersion() != null)
			activity = "build #" + build.getNumber() + " (" + build.getVersion() + ")";
		else
			activity = "build #" + build.getNumber();
		activity += " is " + build.getStatus().getDisplayName();
		if (withEntity)
			activity += " for pull request " + verification.getRequest().getNumberAndTitle();
		return activity;
	}

}
