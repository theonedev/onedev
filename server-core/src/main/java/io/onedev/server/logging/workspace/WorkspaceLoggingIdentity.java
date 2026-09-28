package io.onedev.server.logging.workspace;

import java.io.File;

import io.onedev.server.OneDev;
import io.onedev.server.logging.LoggingIdentity;
import io.onedev.server.model.Workspace;
import io.onedev.server.workspace.WorkspaceService;

class WorkspaceLoggingIdentity implements LoggingIdentity {

	private static final long serialVersionUID = 1L;

	private final Long projectId;

	private final Long workspaceNumber;

	WorkspaceLoggingIdentity(Long projectId, Long workspaceNumber) {
		this.projectId = projectId;
		this.workspaceNumber = workspaceNumber;
	}

	Long getProjectId() {
		return projectId;
	}

	Long getWorkspaceNumber() {
		return workspaceNumber;
	}

	@Override
	public File getFile() {
		return OneDev.getInstance(WorkspaceService.class).getLogFile(projectId, workspaceNumber);
	}

	@Override
	public String getLockName() {
		return Workspace.getLogLockName(projectId, workspaceNumber);
	}

}
