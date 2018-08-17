package io.onedev.server.event.issue;

import io.onedev.server.model.Issue;

public class IssueCommitted {

	private final Issue issue;
	
	public IssueCommitted(Issue issue) {
		this.issue = issue;
	}

	public Issue getIssue() {
		return issue;
	}

}
