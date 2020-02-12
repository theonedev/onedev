package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.Build;
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

	@Override
	public String getActivity(boolean withEntity) {
		Build build = pullRequestBuild.getBuild();
		String activity = build.getJobName() + " ";
		if (build.getVersion() != null)
			activity = "build #" + build.getNumber() + " (" + build.getVersion() + ")";
		else
			activity = "build #" + build.getNumber();
		activity += " is " + build.getStatus().getDisplayName();
		if (withEntity)
			activity += " for pull request " + pullRequestBuild.getRequest().describe();
		return activity;
	}

}
