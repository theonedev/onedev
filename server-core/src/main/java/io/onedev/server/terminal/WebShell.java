package io.onedev.server.terminal;

import java.io.Serializable;

public abstract class WebShell implements Shell, Serializable {

	private static final long serialVersionUID = 1L;

	private final Long buildId;
	
	private final String sessionId;
	
	public WebShell(Long buildId, String sessionId) {
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