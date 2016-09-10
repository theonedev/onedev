package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.event.MarkdownAware;

public class PullRequestStatusChangeEvent extends PullRequestChangeEvent implements MarkdownAware {

	private final PullRequestStatusChange statusChange;
	
	public PullRequestStatusChangeEvent(PullRequestStatusChange statusChange) {
		super(statusChange.getRequest(), statusChange.getUser(), statusChange.getDate());
		this.statusChange = statusChange;
	}

	public PullRequestStatusChange getStatusChange() {
		return statusChange;
	}

	@Override
	public String getMarkdown() {
		return statusChange.getNote();
	}

}
