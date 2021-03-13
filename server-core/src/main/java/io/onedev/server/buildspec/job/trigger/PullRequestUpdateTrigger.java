package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name="Pull request open or update", description="Job will run on the merge commit of target branch and source branch")
public class PullRequestUpdateTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public SubmitReason triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof PullRequestMergePreviewCalculated) {
			PullRequestMergePreviewCalculated mergePreviewCalculated = (PullRequestMergePreviewCalculated) event;
			PullRequest request = mergePreviewCalculated.getRequest();
			return triggerMatches(request);
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return getTriggerDescription("open/update");
	}

}
