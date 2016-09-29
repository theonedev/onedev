package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;

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
