package io.onedev.server.util;

import java.io.Serializable;

public class MilestoneAndIssueState implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long milestoneId;
	
	private final String issueState;
	
	public MilestoneAndIssueState(Long milestoneId, String issueState) {
		this.milestoneId = milestoneId;
		this.issueState = issueState;
	}

	public Long getMilestoneId() {
		return milestoneId;
	}

	public String getIssueState() {
		return issueState;
	}
	
}
