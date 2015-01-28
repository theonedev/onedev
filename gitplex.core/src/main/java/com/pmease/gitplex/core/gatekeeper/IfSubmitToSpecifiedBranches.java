package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.Lists;
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
@Editable(order=100, icon="fa-ext fa-branch", category=GateKeeper.CATEGROY_CHECK_BRANCH, description=
		"This gate keeper will be passed if the commit is submitted to specified branches.")
public class IfSubmitToSpecifiedBranches extends AbstractGateKeeper {

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
		return checkBranch(request.getTarget().getName());
	}

	@Override
	protected GateKeeper trim(Repository repository) {
		GitPlex.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

	private CheckResult checkBranch(String branchName) {
		List<String> branchNames = new ArrayList<>();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (Long branchId: branchIds)
			branchNames.add(dao.load(Branch.class, branchId).getName());
		
		if (branchIds.size() > 1) {
			if (branchNames.contains(branchName))
				return passed(Lists.newArrayList("Target branch is one of '" + StringUtils.join(branchNames, ", ") + "'."));
			else
				return failed(Lists.newArrayList("Target branch is not any one of '" + StringUtils.join(branchNames, ", ") + "'."));
		} else {
			if (branchNames.contains(branchName))
				return passed(Lists.newArrayList("Target branch is '" + branchNames.get(0) + "'."));
			else
				return failed(Lists.newArrayList("Target branch is not '" + branchNames.get(0) + "'."));
		}
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkBranch(branch.getName());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkBranch(branch.getName());
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		String branchName = Branch.parseName(refName);
		if (branchName != null)
			return checkBranch(branchName);
		else 
			return ignored();
	}

}
