package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.editable.TeamChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.helper.branchselection.SpecifyTargetBranchesByNames;
import com.pmease.gitplex.core.gatekeeper.helper.branchselection.TargetBranchSelection;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-lock", category=GateKeeper.CATEGORY_COMMONLY_USED, 
		description="By default, users with write permission of the repository can push "
				+ "code to all branches. Use this gate keeper to restrict write access of "
				+ "specified branches to specified teams.")
public class BranchProtection extends AbstractGateKeeper {
	
	private TargetBranchSelection branchSelection = new SpecifyTargetBranchesByNames();
	
	private List<Long> teamIds = new ArrayList<>();
	
	@Editable(name="Branches to Be Protected", order=100)
	@Valid
	@NotNull
	public TargetBranchSelection getBranchSelection() {
		return branchSelection;
	}

	public void setBranchSelection(TargetBranchSelection branchSelection) {
		this.branchSelection = branchSelection;
	}

	@Editable(name="Restrict Write Access to Below Teams", order=200)
	@TeamChoice(excludes={Team.ANONYMOUS, Team.LOGGEDIN})
	@Size(min=1)
	@NotNull
	public List<Long> getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(List<Long> teamIds) {
		this.teamIds = teamIds;
	}

	@Override
	protected GateKeeper trim(Repository repository) {
		GitPlex.getInstance(TeamManager.class).trim(teamIds);
		if (teamIds.isEmpty())
			return null;
		else
			return this;
	}

	private GateKeeper getGateKeeper() {
		IfThenGateKeeper ifThenGate = new IfThenGateKeeper();
		ifThenGate.setIfGate(branchSelection.getGateKeeper());
		
		OrGateKeeper orGateKeeper = new OrGateKeeper();
		for (Long teamId: teamIds) {
			IfApprovedBySpecifiedTeam ifApprovedBySpecifiedTeam = new IfApprovedBySpecifiedTeam();
			ifApprovedBySpecifiedTeam.setTeamId(teamId);
			orGateKeeper.getGateKeepers().add(ifApprovedBySpecifiedTeam);
			IfSubmittedBySpecifiedTeam ifSubmittedBySpecifiedTeam = new IfSubmittedBySpecifiedTeam();
			ifSubmittedBySpecifiedTeam.setTeamId(teamId);
			orGateKeeper.getGateKeepers().add(ifSubmittedBySpecifiedTeam);
		}
		ifThenGate.setThenGate(orGateKeeper);
		
		return ifThenGate;
	}

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return getGateKeeper().checkRequest(request);
	}

	@Override
	protected CheckResult doCheckFile(User user, Repository repository, String branch, String file) {
		return getGateKeeper().checkFile(user, repository, branch, file);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Repository repository, String branch, String commit) {
		return getGateKeeper().checkCommit(user, repository, branch, commit);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return getGateKeeper().checkRef(user, repository, refName);
	}

}
