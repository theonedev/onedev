package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=400, icon="fa-group", category=GateKeeper.CATEGORY_CHECK_APPROVALS, description=
		"This gate keeper will be passed if the commit is approved by majorities of specified team.")
public class IfApprovedByMajoritiesOfSpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		CheckResult result = getGateKeeper().checkRequest(request);
		
		if (result instanceof Passed)
			result = passed(Lists.newArrayList("Approved by majorities of team '" + getTeam().getName() + "'."));
		else if (result instanceof Failed)
			result = failed(Lists.newArrayList("Not approved by majorities of team '" + getTeam().getName() + "'."));
		
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
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		CheckResult result = getGateKeeper().checkFile(user, depot, branch, file);
		
		if (result instanceof Passed)
			result = passed(Lists.newArrayList("Approved by majorities of team '" + getTeam().getName() + "'."));
		else if (result instanceof Failed)
			result = failed(Lists.newArrayList("Not approved by majorities of team '" + getTeam().getName() + "'."));
		
		return result;
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		CheckResult result = getGateKeeper().checkPush(user, depot, refName, oldCommit, newCommit);
		
		if (result instanceof Passed)
			result = passed(Lists.newArrayList("Approved by majorities of team '" + getTeam().getName() + "'."));
		else if (result instanceof Failed)
			result = failed(Lists.newArrayList("Not approved by majorities of team '" + getTeam().getName() + "'."));
		
		return result;
	}
}
