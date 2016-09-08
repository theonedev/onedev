package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;

public abstract class PullRequestStatusChangeEvent extends PullRequestChangeEvent implements MarkdownAware {

	private final String note;
	
	public PullRequestStatusChangeEvent(PullRequest request, Account user, Date date, @Nullable String note) {
		super(request, user, date);
		this.note = note;
	}

	@Nullable
	public String getNote() {
		return note;
	}

	@Override
	public String getMarkdown() {
		return getNote();
	}

}
