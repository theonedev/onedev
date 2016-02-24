package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.TeamManager;

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

	private Team getTeam(User owner) {
    	return Preconditions.checkNotNull(GitPlex.getInstance(TeamManager.class).findBy(owner, teamName));
    }

	@Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return check(request.getSubmitter(), request.getTargetDepot().getOwner());
    }

	private CheckResult check(User user, User owner) {
		if (user != null) {
			for (Membership membership: user.getMemberships()) {
				if (membership.getTeam().equals(getTeam(owner)))
					return passed(Lists.newArrayList("Submitted by a member of team '" + teamName + "'."));
			}
		}
		return failed(Lists.newArrayList("Not submitted by a member of team '" + teamName + "'."));
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, 
			ObjectId oldCommit, ObjectId newCommit) {
		return check(user, depot.getOwner());
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return check(user, depot.getOwner());
	}

}
