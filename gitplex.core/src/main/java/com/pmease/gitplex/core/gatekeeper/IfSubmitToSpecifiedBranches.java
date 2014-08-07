package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.gitplex.core.GitPlex;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.editable.BranchChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-branch", description=
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
	protected GateKeeper trim(Repository repository) {
		GitPlex.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

	private CheckResult checkBranch(Branch branch) {
		List<String> branchNames = new ArrayList<>();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (Long branchId: branchIds)
			branchNames.add(dao.load(Branch.class, branchId).getName());
		
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
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
