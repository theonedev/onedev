package com.gitplex.server.core.event.pullrequest;

import java.util.Date;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.CodeComment;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.event.MarkdownAware;

@Editable(name="commented code")
public class PullRequestCodeCommentCreated extends PullRequestCodeCommentEvent implements MarkdownAware {

	public PullRequestCodeCommentCreated(PullRequest request, CodeComment comment) {
		super(request, comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public Account getUser() {
		return getComment().getUser();
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

}
