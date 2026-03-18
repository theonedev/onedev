package io.onedev.server.ai.dispatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;

@Singleton
public class CommentDispatchListener {

	private final AiDispatchManager manager;

	@Inject
	public CommentDispatchListener(AiDispatchManager manager) {
		this.manager = manager;
	}

	@Listen
	public void on(PullRequestCommentCreated event) {
		manager.dispatch(event.getComment());
	}

}
