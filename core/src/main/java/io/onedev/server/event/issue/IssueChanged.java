package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;

public class IssueChanged extends IssueEvent implements MarkdownAware {

	private final IssueChange change;
	
	public IssueChanged(IssueChange change) {
		super(change.getIssue());
		this.change = change;
	}

	public IssueChange getChange() {
		return change;
	}

	@Override
	public User getUser() {
		return User.getForDisplay(getChange().getUser(), getChange().getUserName());
	}

	@Override
	public Date getDate() {
		return change.getDate();
	}

	@Override
	public String getTitle() {
		return change.getData().getTitle(change, true);
	}

	@Override
	public String describeAsHtml() {
		return change.getData().describeAsHtml(change);
	}

	@Override
	public String getMarkdown() {
		return change.getData().getComment();
	}

}
