package com.gitplex.core.event.codecomment;

import java.util.Date;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.support.CodeCommentActivity;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;

public abstract class CodeCommentActivityEvent extends CodeCommentEvent {

	private final CodeCommentActivity activity;
	
	public CodeCommentActivityEvent(CodeCommentActivity activity) {
		super(activity.getComment());
		this.activity = activity;
	}

	public CodeCommentActivity getActivity() {
		return activity;
	}

	@Override
	public String getMarkdown() {
		return activity.getNote();
	}

	@Override
	public Account getUser() {
		return activity.getUser();
	}

	@Override
	public Date getDate() {
		return activity.getDate();
	}

	public abstract PullRequestCodeCommentActivityEvent getPullRequestCodeCommentActivityEvent(PullRequest request);
	
}
