package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;

@Editable(order=500, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is submitted by a member of specified team.")
public class IfSubmittedBySpecifiedTeam extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private String teamName;

	@Editable(name="Team", order=100)
    public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return checkSubmitter(request.getSubmitter(), request.getTargetDepot().getOwner());
    }

	private CheckResult checkSubmitter(Account user, Account owner) {
		if (user != null) {
	    	for (Membership membership: owner.getUserMemberships()) {
	    		if (membership.getJoinedTeams().contains(teamName)) {
	    			if (membership.getUser().equals(user)) {
						return passed(Lists.newArrayList("Submitted by a member of team " + teamName));
	    			}
	    		}
	    	}
		}
		return failed(Lists.newArrayList("Not submitted by a member of team " + teamName));
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldCommit, ObjectId newCommit) {
		return checkSubmitter(user, depot.getOwner());
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return checkSubmitter(user, depot.getOwner());
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
		if (teamName.equals(oldName))
			teamName = newName;
	}

	@Override
	public boolean onTeamDelete(String teamName) {
		return this.teamName.equals(teamName);
	}

	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account originalOwner) {
		return depotDefiningGateKeeper.equals(transferredDepot);
	}

}
