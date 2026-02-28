package io.onedev.server.event.project.workspace;

import io.onedev.server.model.Workspace;

public class WorkspaceActive extends WorkspaceEvent {

	private static final long serialVersionUID = 1L;

	public WorkspaceActive(Workspace workspace) {
		super(workspace.getUser(), workspace.getActiveDate(), workspace);
	}

	@Override
	public String getActivity() {
		return "active";
	}

}
