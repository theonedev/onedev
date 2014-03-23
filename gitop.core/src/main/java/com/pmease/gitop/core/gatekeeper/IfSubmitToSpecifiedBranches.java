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
import com.pmease.gitop.model.Repository;
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
		return checkBranch(request.getTarget());
	}

	@Override
	protected GateKeeper trim(Repository project) {
		Gitop.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

	private CheckResult checkBranch(Branch branch) {
		List<String> branchNames = new ArrayList<>();
		BranchManager branchManager = Gitop.getInstance(BranchManager.class);
		for (Long branchId: branchIds)
			branchNames.add(branchManager.load(branchId).getName());
		
		if (branchIds.size() > 1) {
			if (branchIds.contains(branch.getId()))
				return approved("Target branch is one of '" + StringUtils.join(branchNames, ", ") + "'.");
			else
				return disapproved("Target branch is not any one of '" + StringUtils.join(branchNames, ", ") + "'.");
		} else {
			if (branchIds.contains(branch.getId()))
				return approved("Target branch is '" + branchNames.get(0) + "'.");
			else
				return disapproved("Target branch is not '" + branchNames.get(0) + "'.");
		}
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkBranch(branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkBranch(branch);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository project, String refName) {
		return ignored();
	}

}
