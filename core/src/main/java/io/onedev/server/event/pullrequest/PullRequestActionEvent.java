package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.User;

public class PullRequestActionEvent extends PullRequestEvent implements MarkdownAware {

	private final PullRequestAction action;
	
	public PullRequestActionEvent(PullRequestAction action) {
		super(action.getRequest());
		this.action = action;
	}

	public PullRequestAction getAction() {
		return action;
	}

	@Override
	public User getUser() {
		return action.getUser();
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
