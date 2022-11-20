package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;

public class PullRequestBuildEvent extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long buildId;
	
	public PullRequestBuildEvent(Build build) {
		super(null, new Date(), build.getRequest());
		buildId = build.getId();
	}

	public Build getBuild() {
		return OneDev.getInstance(BuildManager.class).load(buildId);
	}

	@Override
	public String getActivity() {
		Build build = getBuild();
		String activity = build.getJobName() + " ";
		if (build.getVersion() != null)
			activity = "build #" + build.getNumber() + " (" + build.getVersion() + ")";
		else
			activity = "build #" + build.getNumber();
		activity += " is " + build.getStatus();
		return activity;
	}

}
