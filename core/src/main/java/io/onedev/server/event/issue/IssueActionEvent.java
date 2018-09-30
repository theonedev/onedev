package io.onedev.server.event.issue;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueAction;

public class IssueActionEvent extends IssueEvent implements MarkdownAware {

	private final IssueAction action;
	
	public IssueActionEvent(IssueAction action) {
		super(action.getUser(), action.getDate(), action.getIssue());
		this.action = action;
	}

	public IssueAction getAction() {
		return action;
	}

	@Override
	public String getMarkdown() {
		if (action.getData().getCommentSupport() != null)
			return action.getData().getCommentSupport().getComment();
		else
			return null;
	}

}
