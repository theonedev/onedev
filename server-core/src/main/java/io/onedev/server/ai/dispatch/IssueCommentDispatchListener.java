package io.onedev.server.ai.dispatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.issue.IssueCommentCreated;

@Singleton
public class IssueCommentDispatchListener {

	private final AiDispatchManager manager;

	@Inject
	public IssueCommentDispatchListener(AiDispatchManager manager) {
		this.manager = manager;
	}

	@Listen
	public void on(IssueCommentCreated event) {
		manager.dispatch(event.getComment());
	}

}
