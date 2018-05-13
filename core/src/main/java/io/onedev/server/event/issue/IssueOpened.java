package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(name="opened")
public class IssueOpened extends IssueEvent implements MarkdownAware {

	public IssueOpened(Issue issue) {
		super(issue);
	}

	@Override
	public String getMarkdown() {
		return getIssue().getDescription();
	}

	@Override
	public User getUser() {
		return User.getForDisplay(getIssue().getSubmitter(), getIssue().getSubmitterName());
	}

	@Override
	public Date getDate() {
		return getIssue().getSubmitDate();
	}

}
