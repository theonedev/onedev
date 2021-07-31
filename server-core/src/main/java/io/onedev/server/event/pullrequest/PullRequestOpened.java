package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequest;

public class PullRequestOpened extends PullRequestEvent {

	public PullRequestOpened(PullRequest request) {
		super(request.getSubmitter(), request.getSubmitDate(), request);
	}

	@Override
	public String getMarkdown() {
		return getRequest().getDescription();
	}

	@Override
	public String getActivity() {
		return "opened";
	}

}
