package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.core.gatekeeper.helper.branchselection.SpecifyTargetBranchesByIds;
import com.pmease.gitop.core.gatekeeper.helper.branchselection.TargetBranchSelection;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.CommonGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.OrGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=100, icon="icon-lock", description="By default, users with write permission of "
		+ "the repository can push code to all branches. Use this gate keeper to restrict write "
		+ "access of specified branches to specified teams.")
public class BranchProtection extends CommonGateKeeper {
	
	private TargetBranchSelection branchSelection = new SpecifyTargetBranchesByIds();
	
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
		if (branchSelection.trim(repository) == null)
			return null;
		
		Gitop.getInstance(TeamManager.class).trim(teamIds);
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
		}
		ifThenGate.setThenGate(orGateKeeper);
		
		return ifThenGate;
	}

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return getGateKeeper().checkRequest(request);
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return getGateKeeper().checkFile(user, branch, file);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return getGateKeeper().checkCommit(user, branch, commit);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return getGateKeeper().checkRef(user, repository, refName);
	}

}
