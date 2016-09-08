package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.event.MarkdownAware;

@Editable(name="commented")
public class PullRequestCommented extends PullRequestChangeEvent implements MarkdownAware {

	private final PullRequestComment comment;
	
	public PullRequestCommented(PullRequestComment comment) {
		super(comment.getRequest(), comment.getUser(), comment.getDate());
		this.comment = comment;
	}

	public PullRequestComment getComment() {
		return comment;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

}
