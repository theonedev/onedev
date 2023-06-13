package io.onedev.server.web.websocket;

import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.model.Issue;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IssueEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public IssueEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(IssueEvent event) {
		webSocketManager.notifyObservablesChange(event.getIssue().getChangeObservables(event.affectsListing()), event.getSourcePage());
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			var project = ((Issue) event.getEntity()).getProject();
			webSocketManager.notifyObservablesChange(project.getIssueListObservables(), event.getSourcePage());
		}
	}
	
}