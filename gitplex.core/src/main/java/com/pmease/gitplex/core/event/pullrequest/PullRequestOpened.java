package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="opened")
public class PullRequestOpened extends PullRequestChangeEvent {

	public PullRequestOpened(PullRequest request) {
		super(request);
	}

}
