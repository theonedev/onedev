package io.onedev.server.util;

import java.io.Serializable;

public class ProjectPackTypeStat implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final String type;
	
	private final long typeCount;
	
	public ProjectPackTypeStat(Long projectId, String type, long typeCount) {
		this.projectId = projectId;
		this.type = type;
		this.typeCount = typeCount;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getType() {
		return type;
	}

	public long getTypeCount() {
		return typeCount;
	}
}
