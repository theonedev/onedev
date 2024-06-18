package io.onedev.server.util;

import java.io.Serializable;

public class IterationAndIssueState implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long iterationId;
	
	private final String issueState;
	
	public IterationAndIssueState(Long iterationId, String issueState) {
		this.iterationId = iterationId;
		this.issueState = issueState;
	}

	public Long getIterationId() {
		return iterationId;
	}

	public String getIssueState() {
		return issueState;
	}
	
}
