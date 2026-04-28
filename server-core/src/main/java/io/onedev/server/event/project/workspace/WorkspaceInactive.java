package io.onedev.server.event.project.workspace;

import io.onedev.server.model.Workspace;

public class WorkspaceInactive extends WorkspaceEvent {

	private static final long serialVersionUID = 1L;

	public WorkspaceInactive(Workspace workspace) {
		super(workspace.getUser(), workspace.getInactiveDate(), workspace);
	}

	@Override
	public String getActivity() {
		return "inactive";
	}

}
