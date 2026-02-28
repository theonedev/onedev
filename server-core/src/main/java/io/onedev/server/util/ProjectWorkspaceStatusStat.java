package io.onedev.server.util;

import java.io.Serializable;

import io.onedev.server.model.Workspace.Status;

public class ProjectWorkspaceStatusStat implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final Status workspaceStatus;
	
	private final long statusCount;
	
	public ProjectWorkspaceStatusStat(Long projectId, Status workspaceStatus, long statusCount) {
		this.projectId = projectId;
		this.workspaceStatus = workspaceStatus;
		this.statusCount = statusCount;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Status getWorkspaceStatus() {
		return workspaceStatus;
	}

	public long getStatusCount() {
		return statusCount;
	}

}
