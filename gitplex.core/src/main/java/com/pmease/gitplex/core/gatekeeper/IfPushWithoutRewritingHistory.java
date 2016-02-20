package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Editable(order=2000, icon="fa-ext fa-repo-lock", description="This gate keeper will be passed if the push "
		+ "operation does not rewrite history of target branch. Rewriting history of public branches "
		+ "is dangerous, and it happens when user forces a push without merging/rebasing with the "
		+ "branch head.")
@SuppressWarnings("serial")
public class IfPushWithoutRewritingHistory extends AbstractGateKeeper {

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (!oldCommit.equals(ObjectId.zeroId()) && !newCommit.equals(ObjectId.zeroId())) {
			if (depot.isAncestor(oldCommit.name(), newCommit.name()))
				return passed(Lists.newArrayList("Push operation does not rewrite history."));
			else
				return failed(Lists.newArrayList("Push operation rewrites history."));
		} else {
			return ignored();
		}
	}

}
