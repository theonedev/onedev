package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.annotation.Editable;

@Editable(order=320, name="Pull request discard", description="Job will run on head commit of target branch")
public class PullRequestDiscardTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
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
