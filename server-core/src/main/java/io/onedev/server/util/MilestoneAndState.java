package io.onedev.server.util;

import io.onedev.server.model.Milestone;

public class MilestoneAndState extends Pair<Milestone, String> {

	private final Long milestoneId;
	
	private final String issueState;
	
	public MilestoneAndState(Long milestoneId, String issueState) {
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
