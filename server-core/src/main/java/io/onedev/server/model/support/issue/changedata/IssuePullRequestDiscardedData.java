package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.PullRequest;

public class IssuePullRequestDiscardedData extends IssuePullRequestData {

	private static final long serialVersionUID = 1L;
	
	public IssuePullRequestDiscardedData(PullRequest request) {
		super(request);
	}
	
	@Override
	public String getDescription() {
		return "discarded pull request of this issue";
	}
	
}
