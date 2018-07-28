package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.User;

public class IssueActionEvent extends IssueEvent implements MarkdownAware {

	private final IssueAction action;
	
	public IssueActionEvent(IssueAction action) {
		super(action.getIssue());
		this.action = action;
	}

	public IssueAction getAction() {
		return action;
	}

	@Override
	public User getUser() {
		return User.getForDisplay(getAction().getUser(), getAction().getUserName());
	}

	@Override
	public Date getDate() {
		return action.getDate();
	}

	@Override
	public String getMarkdown() {
		if (action.getData().getCommentSupport() != null)
			return action.getData().getCommentSupport().getComment();
		else
			return null;
	}

}
