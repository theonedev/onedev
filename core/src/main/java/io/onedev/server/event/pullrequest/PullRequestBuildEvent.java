package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;

public class PullRequestBuildEvent extends PullRequestEvent {

	private final Build build;
	
	public PullRequestBuildEvent(PullRequest request, Build build) {
		super(null, new Date(), request);
		this.build = build;
	}

	public Build getBuild() {
		return build;
	}

}
