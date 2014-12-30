package com.pmease.gitplex.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Editable(order=100, icon="pa-repo-locked", description="This gate keeper will be passed if the push operation does not rewrite history "
		+ "of target branch. Rewriting history of public branches is dangerous, and it happens when user forces "
		+ "a push without merging/rebasing with the branch head.")
@SuppressWarnings("serial")
public class IfPushWithoutRewritingHistory extends AbstractGateKeeper {

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		if (branch.getRepository().git().isAncestor(branch.getHeadCommitHash(), commit))
			return passed("Push operation does not rewrite history.");
		else
			return failed("Push operation rewrites history.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
