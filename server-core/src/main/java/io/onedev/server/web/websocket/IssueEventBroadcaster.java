package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.loader.Listen;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;

@Singleton
public class IssueEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public IssueEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(IssueEvent event) {
		webSocketManager.notifyObservableChange(Issue.getWebSocketObservable(event.getIssue().getId()));
		if (event.affectsBoards()) {
			Project project = event.getIssue().getProject();
			do {
				webSocketManager.notifyObservableChange(BoardSpec.getWebSocketObservable(project.getId()));
				project = project.getParent();
			} while (project != null);
		}
	}

}