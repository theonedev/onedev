package io.onedev.server.event.project.workspace;

import java.io.Serial;
import java.util.Date;

import io.onedev.server.model.Workspace;

public class WorkspaceTerminalOpened extends WorkspaceEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	public WorkspaceTerminalOpened(Workspace workspace) {
		super(workspace.getUser(), new Date(), workspace);
	}

	@Override
	public String getActivity() {
		return "terminal opened";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
