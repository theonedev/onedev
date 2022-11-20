package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=320, name="Pull request discard", description="Job will run on head commit of target branch")
public class PullRequestDiscardTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public SubmitReason triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChangeEvent = (PullRequestChanged) event;
			if (pullRequestChangeEvent.getChange().getData() instanceof PullRequestDiscardData)
				return triggerMatches(pullRequestChangeEvent.getRequest());
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return getTriggerDescription("discard");
	}

}
