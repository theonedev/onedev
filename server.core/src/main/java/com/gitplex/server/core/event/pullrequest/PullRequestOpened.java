package com.gitplex.server.core.event.pullrequest;

import java.util.Date;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.event.MarkdownAware;

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
