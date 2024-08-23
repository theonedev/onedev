package io.onedev.server.model.support.issue.transitionspec;

import io.onedev.server.annotation.Editable;

@Editable(order=300, name="Pull request is discarded")
public class PullRequestDiscardedSpec extends PullRequestSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getTriggerDescription() {
		if (getBranches() != null)
			return "pull request to branches '" + getBranches() + "' is discarded";
		else
			return "pull request to any branch is discarded";
	}

}
