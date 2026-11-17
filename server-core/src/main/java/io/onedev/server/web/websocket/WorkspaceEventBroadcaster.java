package io.onedev.server.web.websocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import io.onedev.server.event.project.workspace.*;
import io.onedev.server.model.Issue;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Workspace;

@Singleton
public class WorkspaceEventBroadcaster {

	@Inject
	private WebSocketService webSocketService;

	@Listen
	public void on(WorkspaceCreated event) {
		if (event.getWorkspace().getIssue() != null)
			notifyIssueWorkspacesChange(Set.of(event.getWorkspace().getIssue().getId()));
		if (event.getWorkspace().getRequest() != null)
			notifyPullRequestWorkspacesChange(Set.of(event.getWorkspace().getRequest().getId()));
	}

	@Listen
	public void on(WorkspaceDeleted event) {
		if (event.getIssueId() != null)
			notifyIssueWorkspacesChange(Set.of(event.getIssueId()));
		if (event.getPullRequestId() != null)
			notifyPullRequestWorkspacesChange(Set.of(event.getPullRequestId()));
	}

	@Listen
	public void on(WorkspacesDeleted event) {
		notifyIssueWorkspacesChange(event.getIssueIds());
		notifyPullRequestWorkspacesChange(event.getPullRequestIds());
	}

	private void notifyIssueWorkspacesChange(Collection<Long> issueIds) {
		var observables = new ArrayList<String>();
		for (var issueId: issueIds)
			observables.add(Workspace.getIssueWorkspacesChangeObservable(issueId));
		if (!observables.isEmpty())
			webSocketService.notifyObservablesChange(observables, null);
	}

	private void notifyPullRequestWorkspacesChange(Collection<Long> requestIds) {
		var observables = new ArrayList<String>();
		for (var requestId: requestIds)
			observables.add(Workspace.getPullRequestWorkspacesChangeObservable(requestId));
		if (!observables.isEmpty())
			webSocketService.notifyObservablesChange(observables, null);
	}

	@Listen
	public void on(WorkspaceTerminalOpened event) {
		webSocketService.notifyObservablesChange(Set.of(event.getWorkspace().getTerminalChangeObservable()), event.getSourcePage());
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
