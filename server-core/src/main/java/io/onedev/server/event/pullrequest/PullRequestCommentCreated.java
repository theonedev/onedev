package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestComment;

public class PullRequestCommentCreated extends PullRequestEvent implements MarkdownAware {

	private final PullRequestComment comment;
	
	public PullRequestCommentCreated(PullRequestComment comment) {
		super(comment.getUser(), comment.getDate(), comment.getRequest());
		this.comment = comment;
	}

	public PullRequestComment getComment() {
		return comment;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "commented";
		if (withEntity)
			activity += " on pull request " + getRequest().getNumberAndTitle();
		return activity;
	}

}
