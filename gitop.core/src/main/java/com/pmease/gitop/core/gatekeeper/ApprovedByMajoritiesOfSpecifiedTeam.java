package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class ApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		GetMinScoreFromSpecifiedTeam gateKeeper = new GetMinScoreFromSpecifiedTeam();
		gateKeeper.setMinScore(1);
		gateKeeper.setRequireVoteOfAllMembers(true);
		gateKeeper.setTeamId(getTeamId());
		
		return gateKeeper.check(request);
	}

}
