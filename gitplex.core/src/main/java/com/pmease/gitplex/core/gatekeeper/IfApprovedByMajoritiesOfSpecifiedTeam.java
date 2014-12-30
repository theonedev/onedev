package com.pmease.gitplex.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=400, icon="pa-group-o", description=
		"This gate keeper will be passed if the commit is approved by majorities of specified team.")
public class IfApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		CheckResult result = getGateKeeper().checkRequest(request);
		
		if (result instanceof Passed)
			result = passed("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Failed)
			result = failed("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
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
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		CheckResult result = getGateKeeper().checkFile(user, branch, file);
		
		if (result instanceof Passed)
			result = passed("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Failed)
			result = failed("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		CheckResult result = getGateKeeper().checkCommit(user, branch, commit);
		
		if (result instanceof Passed)
			result = passed("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Failed)
			result = failed("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		CheckResult result = getGateKeeper().checkRef(user, repository, refName);
		
		if (result instanceof Passed)
			result = passed("Approved by majorities of team '" + getTeam().getName() + "'.");
		else if (result instanceof Failed)
			result = failed("Not approved by majorities of team '" + getTeam().getName() + "'.");
		
		return result;
	}

}
