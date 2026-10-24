package io.onedev.server.event.project.workspace;

import java.io.Serial;

import io.onedev.server.model.Workspace;

public class WorkspaceCreated extends WorkspaceEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	public WorkspaceCreated(Workspace workspace) {
		super(workspace.getUser(), workspace.getCreateDate(), workspace);
	}

	@Override
	public String getActivity() {
		return "created";
	}

}
