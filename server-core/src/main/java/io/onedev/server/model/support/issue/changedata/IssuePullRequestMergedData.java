package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.PullRequest;

public class IssuePullRequestMergedData extends IssuePullRequestData {

	private static final long serialVersionUID = 1L;
	
	public IssuePullRequestMergedData(PullRequest request) {
		super(request);
	}
	
	@Override
	public String getDescription() {
		return "merged pull request fixing this issue";
	}
	
}
