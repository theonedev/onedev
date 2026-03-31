package io.onedev.server.event.project.workspace;

import java.util.Date;

import io.onedev.server.model.Workspace;

public class WorkspacePending extends WorkspaceEvent {

	private static final long serialVersionUID = 1L;

	public WorkspacePending(Workspace workspace) {
		super(workspace.getUser(), new Date(), workspace);
	}

	@Override
	public String getActivity() {
		return "pending";
	}

}
