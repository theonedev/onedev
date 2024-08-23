package io.onedev.server.model.support.issue.transitionspec;

import io.onedev.server.annotation.Editable;

@Editable(order=250, name="Pull request is merged")
public class PullRequestMergedSpec extends PullRequestSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getTriggerDescription() {
		if (getBranches() != null)
			return "pull request to branches '" + getBranches() + "' is merged";
		else
			return "pull request to any branch is merged";
	}

}
