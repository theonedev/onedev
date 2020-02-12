package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

public class IssuePullRequestOpenedData extends IssuePullRequestData {

	private static final long serialVersionUID = 1L;
	
	public IssuePullRequestOpenedData(PullRequest request) {
		super(request);
	}
	
	@Override
	public String getActivity(Issue withIssue) {
		if (withIssue != null)
			return "opened pull request fixing issue " + withIssue.describe();
		else
			return "opened pull request fixing this issue";
	}
	
}
