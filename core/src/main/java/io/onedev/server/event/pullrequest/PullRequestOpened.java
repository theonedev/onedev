package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

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
