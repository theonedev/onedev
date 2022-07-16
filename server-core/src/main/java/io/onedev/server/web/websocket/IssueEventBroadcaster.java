package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.loader.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

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
		if (event.affectsListing()) 
			notifyIssueListObservableChange(event.getIssue().getProject());
	}

	private void notifyIssueListObservableChange(Project project) {
		do {
			webSocketManager.notifyObservableChange(Issue.getListWebSocketObservable(project.getId()));
			project = project.getParent();
		} while (project != null);
	}
	
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) 
			notifyIssueListObservableChange(((Issue) event.getEntity()).getProject());
	}
	
}