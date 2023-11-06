package io.onedev.server.web.websocket;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.ProjectsPendingDelete;
import io.onedev.server.model.Project;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;

import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class ProjectDeleteEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public ProjectDeleteEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(ProjectsPendingDelete event) {
		Collection<String> observables = new HashSet<>();
		for (var projectId: event.getProjectIds())
			observables.add(Project.getDeleteChangeObservable(projectId));
		webSocketManager.notifyObservablesChange(observables, event.getSourcePage());
	}

	@Listen
	public void on(ProjectDeleted event) {
		webSocketManager.notifyObservablesChange(
				newHashSet(Project.getDeleteChangeObservable(event.getProjectId())), 
				event.getSourcePage());
	}
	
}