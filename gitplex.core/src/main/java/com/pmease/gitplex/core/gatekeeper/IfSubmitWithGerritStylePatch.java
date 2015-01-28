package com.pmease.gitplex.core.gatekeeper;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Editable(order=150, icon="fa-ext fa-file-diff", description="This gate keeper will be passed if the pull request "
		+ "is submitted with gerrit style patch, that is, only one commit is submitted for review at "
		+ "a time, and subsequent updates to the review should be done by amending previous commit.")
@SuppressWarnings("serial")
public class IfSubmitWithGerritStylePatch extends AbstractGateKeeper {

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		String branchHead = request.getBaseCommitHash();
		String requestHead = request.getLatestUpdate().getHeadCommitHash();
		if (request.git().log(branchHead, requestHead, null, 0, 0).size() > 1) {
			return failed(Lists.newArrayList("Please squash/rebase your commits."));
		} else {
			return passed(Lists.newArrayList("No more than one commit."));
		}
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
