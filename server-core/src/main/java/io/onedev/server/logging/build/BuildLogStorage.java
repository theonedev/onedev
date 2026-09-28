package io.onedev.server.logging.build;

import java.io.File;
import java.io.Serializable;

import io.onedev.server.model.Build;

/** Storage directory and shared lock for all stage logs of a build. */
public class BuildLogStorage implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;

	private final Long buildNumber;

	public BuildLogStorage(Long projectId, Long buildNumber) {
		this.projectId = projectId;
		this.buildNumber = buildNumber;
	}

	public File getDirectory() {
		return Build.getLogDir(projectId, buildNumber);
	}

	public String getLockName() {
		return Build.getLogLockName(projectId, buildNumber);
	}

}
