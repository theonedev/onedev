package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
public class ApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		GetMinScoreFromSpecifiedTeam gateKeeper = new GetMinScoreFromSpecifiedTeam();
		gateKeeper.setMinScore(1);
		gateKeeper.setRequireVoteOfAllMembers(true);
		gateKeeper.setTeamId(getTeamId());
		
		CheckResult result = gateKeeper.check(request);
		
		if (result.isAccept())
			result = accept("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result.isReject())
			result = reject("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

}
