package io.onedev.server.event.issue;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.Issue;

public class IssueOpened extends IssueEvent implements MarkdownAware {

	public IssueOpened(Issue issue) {
		super(issue.getSubmitter(), issue.getSubmitDate(), issue);
	}

	@Override
	public String getMarkdown() {
		return getIssue().getDescription();
	}

}