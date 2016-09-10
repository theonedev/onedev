package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;

public class PullRequestCodeCommentEvent extends PullRequestChangeEvent {

	private final CodeComment comment;
	
	public PullRequestCodeCommentEvent(PullRequest request, Account user, Date date, CodeComment comment) {
		super(request, user, date);
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
