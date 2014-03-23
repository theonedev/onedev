package com.pmease.gitop.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.helper.branchselection.SpecifyTargetBranchesByIds;
import com.pmease.gitop.core.gatekeeper.helper.branchselection.TargetBranchSelection;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.CommonGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable(icon="icon-lock", order=300, description="This gate keeper can be used to configure certain branches "
		+ "to only accept commits passing specified build criteria.")
@SuppressWarnings("serial")
public class BuildVerificationOfBranch extends CommonGateKeeper {

	private TargetBranchSelection branchSelection = new SpecifyTargetBranchesByIds();
	
	private int buildCount = 1;
	
	private boolean checkMerged = true;
	
	private boolean blockMode = true;
	
	@Editable(name="Branches to Be Checked", order=50)
	@Valid
	@NotNull
	public TargetBranchSelection getBranchSelection() {
		return branchSelection;
	}

	public void setBranchSelection(TargetBranchSelection branchSelection) {
		this.branchSelection = branchSelection;
	}

	@Override
	protected GateKeeper trim(Repository project) {
		if (branchSelection.trim(project) == null)
			return null;
		else
			return this;
	}

	@Editable(order=100, description="For each specified branch, this specified number of builds has to be "
			+ "reported successful for this gate keeper to be passed. Normally this number represents "
			+ "number of build configurations setting up to verify the branch.")
	@Min(1)
	public int getBuildCount() {
		return buildCount;
	}

	public void setBuildCount(int buildCount) {
		this.buildCount = buildCount;
	}

	@Editable(order=200, description="Enable this to check the merged commit instead of head commit of the "
			+ "pull request.")
	public boolean isCheckMerged() {
		return checkMerged;
	}

	public void setCheckMerged(boolean checkMerged) {
		this.checkMerged = checkMerged;
	}

	@Editable(order=300, description="If this is checked, subsequent gate keepers will not be checked "
			+ "while waiting for the build results. This can be used to only notify relevant voters "
			+ "when the commit passes build.")
	public boolean isBlockMode() {
		return blockMode;
	}

	public void setBlockMode(boolean blockMode) {
		this.blockMode = blockMode;
	}
	
	private GateKeeper getGateKeeper() {
		IfThenGateKeeper ifThenGate = new IfThenGateKeeper();
		ifThenGate.setIfGate(branchSelection.getGateKeeper());
		IfVerifiedByBuilds thenGate = new IfVerifiedByBuilds();
		thenGate.setBlockMode(blockMode);
		thenGate.setBuildCount(buildCount);
		thenGate.setCheckMerged(checkMerged);
		ifThenGate.setThenGate(thenGate);
		
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
	protected CheckResult doCheckRef(User user, Repository project, String refName) {
		return getGateKeeper().checkRef(user, project, refName);
	}

}
