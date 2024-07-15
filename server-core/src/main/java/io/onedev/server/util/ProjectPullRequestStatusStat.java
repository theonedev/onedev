package io.onedev.server.util;

import java.io.Serializable;

import io.onedev.server.model.PullRequest.Status;

public class ProjectPullRequestStatusStat implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final Status pullRequestStatus;
	
	private final long statusCount;
	
	public ProjectPullRequestStatusStat(Long projectId, Status pullRequestStatus, long statusCount) {
		this.projectId = projectId;
		this.pullRequestStatus = pullRequestStatus;
		this.statusCount = statusCount;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Status getPullRequestStatus() {
		return pullRequestStatus;
	}

	public long getStatusCount() {
		return statusCount;
	}

}
