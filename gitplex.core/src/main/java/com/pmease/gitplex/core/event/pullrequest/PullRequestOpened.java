package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;

@Editable(name="opened")
public class PullRequestOpened extends PullRequestChangeEvent implements MarkdownAware {

	public PullRequestOpened(PullRequest request) {
		super(request, request.getSubmitter(), request.getSubmitDate());
	}

	@Override
	public String getMarkdown() {
		return getRequest().getDescription();
	}

}
