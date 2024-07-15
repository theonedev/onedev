package io.onedev.server.util;

import java.io.Serializable;

import io.onedev.server.model.Build;

public class ProjectBuildStatusStat implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final Build.Status buildStatus;
	
	private final long statusCount;
	
	public ProjectBuildStatusStat(Long projectId, Build.Status buildStatus, long statusCount) {
		this.projectId = projectId;
		this.buildStatus = buildStatus;
		this.statusCount = statusCount;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Build.Status getBuildStatus() {
		return buildStatus;
	}

	public long getStatusCount() {
		return statusCount;
	}

}
