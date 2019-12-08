package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.PullRequest;

public class IssuePullRequestReopenedData extends IssuePullRequestData {

	private static final long serialVersionUID = 1L;
	
	public IssuePullRequestReopenedData(PullRequest request) {
		super(request);
	}
	
	@Override
	public String getDescription() {
		return "reopened pull request of this issue";
	}
	
}
