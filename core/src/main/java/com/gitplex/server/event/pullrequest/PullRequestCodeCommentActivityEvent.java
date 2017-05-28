package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.model.User;
import com.gitplex.server.model.support.CodeCommentActivity;

public abstract class PullRequestCodeCommentActivityEvent extends PullRequestCodeCommentEvent {

	private final CodeCommentActivity activity;
	
	public PullRequestCodeCommentActivityEvent(CodeCommentActivity activity) {
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
	public User getUser() {
		return activity.getUser();
	}

	@Override
	public Date getDate() {
		return activity.getDate();
	}

}
