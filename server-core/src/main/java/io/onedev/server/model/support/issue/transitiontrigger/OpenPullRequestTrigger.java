package io.onedev.server.model.support.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200, name="Pull request is opened")
public class OpenPullRequestTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public String getDescription() {
		if (getBranches() != null)
			return "Pull request to branches '" + getBranches() + "' is opened";
		else
			return "Pull request to any branch is opened";
	}

}
