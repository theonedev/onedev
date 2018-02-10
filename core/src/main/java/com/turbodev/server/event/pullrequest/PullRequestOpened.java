package com.turbodev.server.event.pullrequest;

import java.util.Date;

import com.turbodev.server.event.MarkdownAware;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="opened")
public class PullRequestOpened extends PullRequestEvent implements MarkdownAware {

	public PullRequestOpened(PullRequest request) {
		super(request);
	}

	@Override
	public String getMarkdown() {
		return getRequest().getDescription();
	}

	@Override
	public User getUser() {
		return User.getForDisplay(getRequest().getSubmitter(), getRequest().getSubmitterName());
	}

	@Override
	public Date getDate() {
		return getRequest().getSubmitDate();
	}

}
