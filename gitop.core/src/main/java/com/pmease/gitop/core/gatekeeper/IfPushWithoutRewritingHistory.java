package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.AbstractGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable(order=100, icon="icon-repo-lock", description="This gate keeper will be passed if the push operation does not rewrite history "
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
		if (branch.getProject().code().isAncestor(branch.getHeadCommit(), commit))
			return approved("Push operation does not rewrite history.");
		else
			return disapproved("Push operation rewrites history.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository project, String refName) {
		return ignored();
	}

}
