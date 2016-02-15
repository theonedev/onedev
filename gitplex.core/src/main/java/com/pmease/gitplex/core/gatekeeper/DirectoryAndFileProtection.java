package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.editable.TeamChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.helper.branchselection.TargetBranchSelection;
import com.pmease.gitplex.core.gatekeeper.helper.pathselection.SpecifyTargetPathsByDirectories;
import com.pmease.gitplex.core.gatekeeper.helper.pathselection.TargetPathSelection;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=200, icon="fa-lock", category=GateKeeper.CATEGORY_COMMONLY_USED, 
		description="By default, users with write permission of the repository can write to "
				+ "all directories/files. Use this gate keeper to restrict write access of "
				+ "certain directories/files of specified branches to certain teams. Note "
				+ "that if branch is not specified, the restriction will apply to all branches.")
public class DirectoryAndFileProtection extends AbstractGateKeeper {
	
	private TargetBranchSelection branchSelection;
	
	private TargetPathSelection pathSelection = new SpecifyTargetPathsByDirectories();
	
	private List<Long> teamIds = new ArrayList<>();

	@Editable(name="Applicable Branches (Optionally)", order=100)
	@Valid
	@Nullable
	public TargetBranchSelection getBranchSelection() {
		return branchSelection;
	}

	public void setBranchSelection(TargetBranchSelection branchSelection) {
		this.branchSelection = branchSelection;
	}
	
	@Editable(name="Directories or Files to Be Protected")
	@Valid
	@NotNull
	public TargetPathSelection getPathSelection() {
		return pathSelection;
	}

	public void setPathSelection(TargetPathSelection pathSelection) {
		this.pathSelection = pathSelection;
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
	protected GateKeeper trim(Depot depot) {
		GitPlex.getInstance(TeamManager.class).trim(teamIds);
		if (teamIds.isEmpty())
			return null;
		else
			return this;
	}

	private GateKeeper getGateKeeper() {
		IfThenGateKeeper ifThenGate = new IfThenGateKeeper();
		ifThenGate.setIfGate(pathSelection.getGateKeeper());
		ifThenGate.setThenGate(getApprovalGate());

		if (branchSelection != null) {
			IfThenGateKeeper resultGate = new IfThenGateKeeper();
			resultGate.setIfGate(branchSelection.getGateKeeper());
			resultGate.setThenGate(ifThenGate);
			return resultGate;
		} else {
			return ifThenGate;
		}
	}
	
	private GateKeeper getApprovalGate() {
		OrGateKeeper approvalGate = new OrGateKeeper();
		for (Long teamId: teamIds) {
			IfApprovedBySpecifiedTeam ifApprovedBySpecifiedTeam = new IfApprovedBySpecifiedTeam();
			ifApprovedBySpecifiedTeam.setTeamId(teamId);
			approvalGate.getGateKeepers().add(ifApprovedBySpecifiedTeam);
			IfSubmittedBySpecifiedTeam ifSubmittedBySpecifiedTeam = new IfSubmittedBySpecifiedTeam();
			ifSubmittedBySpecifiedTeam.setTeamId(teamId);
			approvalGate.getGateKeepers().add(ifSubmittedBySpecifiedTeam);
		}
		return approvalGate;
	}

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return getGateKeeper().checkRequest(request);
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return getGateKeeper().checkFile(user, depot, branch, file);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		return getGateKeeper().checkCommit(user, depot, branch, commit);
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		return getGateKeeper().checkRef(user, depot, refName);
	}

}
