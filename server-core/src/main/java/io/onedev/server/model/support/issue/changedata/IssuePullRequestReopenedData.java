package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

public class IssuePullRequestReopenedData extends IssuePullRequestData {

	private static final long serialVersionUID = 1L;
	
	public IssuePullRequestReopenedData(PullRequest request) {
		super(request);
	}
	
	@Override
	public String getActivity(Issue withIssue) {
		if (withIssue != null)
			return "reopened pull request fixing issue " + withIssue.describe();
		else
			return "reopened pull request fixing this issue";
	}
	
}
