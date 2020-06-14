package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;

public class PullRequestCodeCommentCreated extends PullRequestCodeCommentEvent implements MarkdownAware {

	public PullRequestCodeCommentCreated(PullRequest request, CodeComment comment) {
		super(comment.getUser(), comment.getCreateDate(), request, comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "created code comment"; 
		if (withEntity)
			activity += " in pull request " + getRequest().getNumberAndTitle();
		return activity;
	}

}
