package io.onedev.server.web.websocket;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.workspace.WorkspaceActive;
import io.onedev.server.event.project.workspace.WorkspaceInactive;

@Singleton
public class WorkspaceEventBroadcaster {

	private final WebSocketService webSocketService;

	@Inject
	public WorkspaceEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}

	@Listen
	public void on(WorkspaceActive event) {
		webSocketService.notifyObservablesChange(Set.of(event.getWorkspace().getStatusChangeObservable()), event.getSourcePage());
	}

	@Listen
	public void on(WorkspaceInactive event) {
		webSocketService.notifyObservablesChange(Set.of(event.getWorkspace().getStatusChangeObservable()), event.getSourcePage());
	}

}
