package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="commented code")
public class PullRequestCodeCommented extends PullRequestChangeEvent {

	private final CodeComment comment;
	
	public PullRequestCodeCommented(PullRequest request, CodeComment comment) {
		super(request);
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
