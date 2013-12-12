package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@SuppressWarnings("serial")
@Editable(category=GateKeeper.CATEGORY_APPROVAL, order=400, description=
		"This condition will be satisified if majorities of specified team approves the commit.")
public class ApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(PullRequest request) {
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
