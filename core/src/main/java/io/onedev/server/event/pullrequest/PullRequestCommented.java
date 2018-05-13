package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(name="commented")
public class PullRequestCommented extends PullRequestEvent implements MarkdownAware {

	private final PullRequestComment comment;
	
	public PullRequestCommented(PullRequestComment comment) {
		super(comment.getRequest());
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
	public User getUser() {
		return comment.getUser();
	}

	@Override
	public Date getDate() {
		return comment.getDate();
	}

}
