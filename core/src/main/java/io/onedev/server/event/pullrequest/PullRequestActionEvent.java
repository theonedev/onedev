package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestAction;

public class PullRequestActionEvent extends PullRequestEvent implements MarkdownAware {

	private final PullRequestAction action;
	
	public PullRequestActionEvent(PullRequestAction action) {
		super(action.getUser(), action.getDate(), action.getRequest());
		this.action = action;
	}

	public PullRequestAction getAction() {
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
