package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.pullrequest.PullRequestBuildCommitUpdated;

@Editable(order=300, name="Pull request open or update", description=""
		+ "Job will run on merge commit of target branch and source branch.<br>"
		+ "<b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits "
		+ "with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, "
		+ "<code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>")
public class PullRequestUpdateTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof PullRequestBuildCommitUpdated) {
			PullRequestBuildCommitUpdated pullRequestBuildCommitUpdated = (PullRequestBuildCommitUpdated) event;
			var request = pullRequestBuildCommitUpdated.getRequest();
			if (request.getBuildRequirement().getRequiredJobs().contains(job.getName()) || !SKIP_COMMIT.apply(request.getLatestUpdate().getHeadCommit()))
				return triggerMatches(request);
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return getTriggerDescription("open/update");
	}

}
