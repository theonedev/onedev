package io.onedev.server.model.support.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=250, name="Pull request is merged")
public class MergePullRequestTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public String getDescription() {
		if (getBranches() != null)
			return "Pull request to branches '" + getBranches() + "' is merged";
		else
			return "Pull request to any branch is merged";
	}

}
