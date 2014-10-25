package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import java.util.Date;

import com.pmease.gitplex.core.model.AbstractPullRequestComment;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public abstract class AbstractCommentPullRequest implements PullRequestActivity {

	private Boolean collapsed;

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	public abstract AbstractPullRequestComment getComment();

	@Override
	public User getUser() {
		return getComment().getUser();
	}

	public boolean isCollapsed() {
		if (collapsed == null)
			collapsed = getComment().isResolved();
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

}
