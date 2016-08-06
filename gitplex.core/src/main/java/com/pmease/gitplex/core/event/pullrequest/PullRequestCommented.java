package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestComment;

@Editable(name="commented")
public class PullRequestCommented extends PullRequestChangeEvent {

	private final PullRequestComment comment;
	
	public PullRequestCommented(PullRequestComment comment) {
		super(comment.getRequest());
		this.comment = comment;
	}

	public PullRequestComment getComment() {
		return comment;
	}

}
