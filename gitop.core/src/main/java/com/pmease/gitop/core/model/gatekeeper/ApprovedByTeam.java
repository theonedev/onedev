package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class ApprovedByTeam implements GateKeeper {

	private String teamName;
	
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		return null;
	}

}
