package io.onedev.server.logging;

import java.io.File;

import io.onedev.server.model.Workspace;

public class WorkspaceLoggingIdentity implements LoggingIdentity {

	private static final long serialVersionUID = 1L;

	private final Long projectId;

	private final Long workspaceNumber;

	public WorkspaceLoggingIdentity(Long projectId, Long workspaceNumber) {
		this.projectId = projectId;
		this.workspaceNumber = workspaceNumber;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getWorkspaceNumber() {
		return workspaceNumber;
	}

	@Override
	public File getFile() {
		return Workspace.getLogFile(projectId, workspaceNumber);
	}

	@Override
	public String getLockName() {
		return Workspace.getLogLockName(projectId, workspaceNumber);
	}

	@Override
	public String getCacheKey() {
		return "workspace-log:" + projectId + ":" + workspaceNumber;
	}

}
