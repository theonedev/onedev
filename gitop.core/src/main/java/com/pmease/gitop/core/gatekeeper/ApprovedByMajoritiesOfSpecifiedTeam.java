package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
public class ApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		GetMinScoreFromSpecifiedTeam gateKeeper = new GetMinScoreFromSpecifiedTeam();
		gateKeeper.setMinScore(1);
		gateKeeper.setRequireVoteOfAllMembers(true);
		gateKeeper.setTeamId(getTeamId());
		
		CheckResult result = gateKeeper.check(request);
		
		if (result instanceof Accepted)
			result = accepted("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Rejected)
			result = rejected("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

}
