package io.onedev.server.event.project.workspace;

import io.onedev.server.model.Workspace;

public class WorkspaceError extends WorkspaceEvent {

	private static final long serialVersionUID = 1L;

	public WorkspaceError(Workspace workspace) {
		super(workspace.getUser(), workspace.getErrorDate(), workspace);
	}

	@Override
	public String getActivity() {
		return "error";
	}

}
