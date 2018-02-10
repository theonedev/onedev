package com.turbodev.server.event.pullrequest;

import java.util.Date;

import com.turbodev.server.event.MarkdownAware;
import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="added code comment")
public class PullRequestCodeCommentCreated extends PullRequestCodeCommentEvent implements MarkdownAware {

	public PullRequestCodeCommentCreated(PullRequest request, CodeComment comment, boolean passive) {
		super(request, comment, passive);
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
