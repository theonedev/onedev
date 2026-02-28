package io.onedev.server.job;

import java.io.Serializable;

import io.onedev.server.terminal.Shell;

public abstract class JobShell implements Shell, Serializable {

	private static final long serialVersionUID = 1L;

	private final Long buildId;
	
	private final String sessionId;
	
	public JobShell(Long buildId, String sessionId) {
		this.buildId = buildId;
		this.sessionId = sessionId;
	}

	public Long getBuildId() {
		return buildId;
	}

	public String getSessionId() {
		return sessionId;
	}
	
}