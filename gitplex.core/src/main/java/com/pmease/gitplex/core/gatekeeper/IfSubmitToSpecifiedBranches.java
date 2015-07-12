package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.editable.BranchChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-ext fa-branch", category=GateKeeper.CATEGROY_CHECK_BRANCH, description=
		"This gate keeper will be passed if the commit is submitted to specified branches.")
public class IfSubmitToSpecifiedBranches extends AbstractGateKeeper {

	private List<String> branches = new ArrayList<>();
	
	@Editable(name="Branches", description="Select branches to check.")
	@BranchChoice
	@NotNull
	@Size(min=1)
	public List<String> getBranches() {
		return branches;
	}

	public void setBranches(List<String> branches) {
		this.branches = branches;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return checkBranch(request.getTargetRepo(), request.getTargetBranch());
	}

	private CheckResult checkBranch(Repository repository, String branch) {
		if (branches.size() > 1) {
			if (branches.contains(branch))
				return passed(Lists.newArrayList("Target branch is one of '" + StringUtils.join(branches, ", ") + "'."));
			else
				return failed(Lists.newArrayList("Target branch is not any one of '" + StringUtils.join(branches, ", ") + "'."));
		} else {
			if (branches.contains(branch))
				return passed(Lists.newArrayList("Target branch is '" + branches.get(0) + "'."));
			else
				return failed(Lists.newArrayList("Target branch is not '" + branches.get(0) + "'."));
		}
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Repository repository, String branch, String file) {
		return checkBranch(repository, branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Repository repository, String branch, String commit) {
		return checkBranch(repository, branch);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		String branch = GitUtils.ref2branch(refName);
		if (branch != null)
			return checkBranch(repository, branch);
		else 
			return ignored();
	}

}
