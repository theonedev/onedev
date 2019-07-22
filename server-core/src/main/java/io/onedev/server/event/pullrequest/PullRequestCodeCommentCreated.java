package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;

public class PullRequestCodeCommentCreated extends PullRequestCodeCommentEvent implements MarkdownAware {

	public PullRequestCodeCommentCreated(PullRequest request, CodeComment comment, boolean derived) {
		super(comment.getUser(), comment.getCreateDate(), request, comment, derived);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

}
