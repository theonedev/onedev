package io.onedev.server.web.websocket;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.issue.IssueEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IssueEventBroadcaster {
	
	private final WebSocketService webSocketService;
	
	@Inject
	public IssueEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}

	@Listen
	public void on(IssueEvent event) {
		webSocketService.notifyObservablesChange(event.getIssue().getChangeObservables(event.affectsListing()), event.getSourcePage());
	}
	
}