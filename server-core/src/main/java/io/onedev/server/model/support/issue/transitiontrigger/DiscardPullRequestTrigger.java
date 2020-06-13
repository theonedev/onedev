package io.onedev.server.model.support.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name="Pull request is discarded")
public class DiscardPullRequestTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public String getDescription() {
		if (getBranches() != null)
			return "Pull request to branches '" + getBranches() + "' is discarded";
		else
			return "Pull request to any branch is discarded";
	}

}
