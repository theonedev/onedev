package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestCodeCommentAdded extends PullRequestCodeCommentEvent implements MarkdownAware {

	public PullRequestCodeCommentAdded(PullRequest request, CodeComment comment, boolean derived) {
		super(request, comment, derived);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public User getUser() {
		return getComment().getUser();
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

}
