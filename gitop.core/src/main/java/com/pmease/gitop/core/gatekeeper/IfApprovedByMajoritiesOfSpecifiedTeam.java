package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@SuppressWarnings("serial")
@Editable(order=400, icon="icon-group", description=
		"This gate keeper will be passed if the commit is approved by majorities of specified team.")
public class IfApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheck(PullRequest request) {
		IfGetMinScoreFromSpecifiedTeam gateKeeper = new IfGetMinScoreFromSpecifiedTeam();
		gateKeeper.setMinScore(1);
		gateKeeper.setRequireVoteOfAllMembers(true);
		gateKeeper.setTeamId(getTeamId());
		
		CheckResult result = gateKeeper.doCheck(request);
		
		if (result instanceof Accepted)
			result = accepted("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Rejected)
			result = rejected("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

}
