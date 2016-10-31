package com.gitplex.core.event.pullrequest;

import java.util.Date;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.event.MarkdownAware;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="opened")
public class PullRequestOpened extends PullRequestChangeEvent implements MarkdownAware {

	public PullRequestOpened(PullRequest request) {
		super(request);
	}

	@Override
	public String getMarkdown() {
		return getRequest().getDescription();
	}

	@Override
	public Account getUser() {
		return getRequest().getSubmitter();
	}

	@Override
	public Date getDate() {
		return getRequest().getSubmitDate();
	}

}
