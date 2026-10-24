package io.onedev.server.event.project.workspace;

import java.io.Serial;

import io.onedev.server.model.Workspace;

public class WorkspaceActive extends WorkspaceEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	public WorkspaceActive(Workspace workspace) {
		super(workspace.getUser(), workspace.getActiveDate(), workspace);
	}

	@Override
	public String getActivity() {
		return "active";
	}

}
