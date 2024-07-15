package io.onedev.server.util;

import java.io.Serializable;

public class ProjectIssueStateStat implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final int stateOrdinal;
	
	private final long stateCount;
	
	public ProjectIssueStateStat(Long projectId, int stateOrdinal, long stateCount) {
		this.projectId = projectId;
		this.stateOrdinal = stateOrdinal;
		this.stateCount = stateCount;
	}

	public Long getProjectId() {
		return projectId;
	}

	public int getStateOrdinal() {
		return stateOrdinal;
	}

	public long getStateCount() {
		return stateCount;
	}

}
