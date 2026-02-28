package io.onedev.server.logging;

import java.io.File;

import io.onedev.server.model.Build;

public class BuildLoggingIdentity implements LoggingIdentity {

	private static final long serialVersionUID = 1L;

	private final Long projectId;

	private final Long buildNumber;
	
	public BuildLoggingIdentity(Long projectId, Long buildNumber) {
		this.projectId = projectId;
		this.buildNumber = buildNumber;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getBuildNumber() {
		return buildNumber;
	}

	@Override
	public File getFile() {
		return Build.getLogFile(projectId, buildNumber);
	}

	@Override
	public String getLockName() {
		return Build.getLogLockName(projectId, buildNumber);
	}

	@Override
	public String getCacheKey() {
		return "build-log:" + projectId + ":" + buildNumber;
	}

}