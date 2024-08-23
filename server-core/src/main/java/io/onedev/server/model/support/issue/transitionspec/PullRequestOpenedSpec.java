package io.onedev.server.model.support.issue.transitionspec;

import io.onedev.server.annotation.Editable;

@Editable(order=200, name="Pull request is opened")
public class PullRequestOpenedSpec extends PullRequestSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getTriggerDescription() {
		if (getBranches() != null)
			return "pull request to branches '" + getBranches() + "' is opened";
		else
			return "pull request to any branch is opened";
	}

}
