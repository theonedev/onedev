package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequestBuild;

public class PullRequestBuildEvent extends PullRequestEvent {

	private final PullRequestBuild pullRequestBuild;
	
	public PullRequestBuildEvent(PullRequestBuild pullRequestBuild) {
		super(null, new Date(), pullRequestBuild.getRequest());
		this.pullRequestBuild = pullRequestBuild;
	}

	public PullRequestBuild getPullRequestBuild() {
		return pullRequestBuild;
	}

}
