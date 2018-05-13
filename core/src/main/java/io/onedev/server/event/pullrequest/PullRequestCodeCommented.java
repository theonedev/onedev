package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(name="added code comment")
public class PullRequestCodeCommented extends PullRequestCodeCommentEvent implements MarkdownAware {

	public PullRequestCodeCommented(PullRequest request, CodeComment comment, boolean passive) {
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
