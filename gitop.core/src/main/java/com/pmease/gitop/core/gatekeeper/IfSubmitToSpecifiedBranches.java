package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.BranchChoice;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.BranchGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=100, icon="icon-git-branch", description=
		"This gate keeper will be passed if the commit is submitted to specified branches.")
public class IfSubmitToSpecifiedBranches extends BranchGateKeeper {

	private List<Long> branchIds = new ArrayList<>();
	
	@Editable(name="Branches", description="Select branches to check.")
	@BranchChoice
	@NotNull
	@Size(min=1)
	public List<Long> getBranchIds() {
		return branchIds;
	}

	public void setBranchIds(List<Long> branchIds) {
		this.branchIds = branchIds;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		List<String> branchNames = new ArrayList<>();
		BranchManager branchManager = Gitop.getInstance(BranchManager.class);
		for (Long branchId: branchIds)
			branchNames.add(branchManager.load(branchId).getName());
		
		if (branchIds.contains(request.getTarget().getId()))
			return accepted("Target branch is one of '" + StringUtils.join(branchNames, ", ") + "'.");
		else
			return rejected("Target branch is not one of '" + StringUtils.join(branchNames, ", ") + "'.");
	}

	@Override
	protected GateKeeper trim(Project project) {
		Gitop.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

	private CheckResult checkBranch(User user, Branch branch) {
		List<String> branchNames = new ArrayList<>();
		BranchManager branchManager = Gitop.getInstance(BranchManager.class);
		for (Long branchId: branchIds)
			branchNames.add(branchManager.load(branchId).getName());
		
		if (branchIds.contains(branch.getId()))
			return accepted("Target branch is one of '" + StringUtils.join(branchNames, ", ") + "'.");
		else
			return rejected("Target branch is not one of '" + StringUtils.join(branchNames, ", ") + "'.");
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkBranch(user, branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkBranch(user, branch);
	}

	@Override
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return ignored();
	}

}
