package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.core.gatekeeper.helper.branchselection.TargetBranchSelection;
import com.pmease.gitop.core.gatekeeper.helper.pathselection.SpecifyTargetPathsByDirectories;
import com.pmease.gitop.core.gatekeeper.helper.pathselection.TargetPathSelection;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.CommonGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.OrGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=200, icon="icon-lock", description="By default, users with write permission of "
		+ "the project can write to all directories/files. Use this gate keeper to restrict write "
		+ "access of certain directories/files of specified branches to certain teams. Note that if "
		+ "branch is not specified, the restriction will apply to all branches.")
public class DirectoryAndFileProtection extends CommonGateKeeper {
	
	private TargetBranchSelection branchSelection;
	
	private TargetPathSelection pathSelection = new SpecifyTargetPathsByDirectories();
	
	private List<Long> teamIds = new ArrayList<>();

	@Editable(name="Optionally Specify Applicable Branches", order=100)
	@Valid
	@Nullable
	public TargetBranchSelection getBranchSelection() {
		return branchSelection;
	}

	public void setBranchSelection(TargetBranchSelection branchSelection) {
		this.branchSelection = branchSelection;
	}
	
	@Editable(name="Specify Directory or File to Protect")
	@Valid
	@NotNull
	public TargetPathSelection getPathSelection() {
		return pathSelection;
	}

	public void setPathSelection(TargetPathSelection pathSelection) {
		this.pathSelection = pathSelection;
	}

	@Editable(name="Restrict Write Access of Above Paths to Below Teams", order=200)
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
	protected GateKeeper trim(Project project) {
		if (branchSelection.trim(project) == null)
			return null;
		
		Gitop.getInstance(TeamManager.class).trim(teamIds);
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
		}
		return approvalGate;
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
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return getGateKeeper().checkRef(user, project, refName);
	}
}
