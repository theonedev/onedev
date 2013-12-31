package com.pmease.gitop.core.gatekeeper;

import javax.annotation.Nullable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@SuppressWarnings("serial")
@Editable(order=400, icon="icon-group", description=
		"This gate keeper will be passed if the commit is approved by majorities of specified team.")
public class IfApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		CheckResult result = getGateKeeper().checkRequest(request);
		
		if (result instanceof Accepted)
			result = accepted("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Rejected)
			result = rejected("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

	private GateKeeper getGateKeeper() {
		IfGetMinScoreFromSpecifiedTeam gateKeeper = new IfGetMinScoreFromSpecifiedTeam();
		gateKeeper.setMinScore(1);
		gateKeeper.setRequireVoteOfAllMembers(true);
		gateKeeper.setTeamId(getTeamId());
		return gateKeeper;
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, @Nullable String file) {
		CheckResult result = getGateKeeper().checkFile(user, branch, file);
		
		if (result instanceof Accepted)
			result = accepted("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Rejected)
			result = rejected("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		CheckResult result = getGateKeeper().checkCommit(user, branch, commit);
		
		if (result instanceof Accepted)
			result = accepted("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Rejected)
			result = rejected("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

}
