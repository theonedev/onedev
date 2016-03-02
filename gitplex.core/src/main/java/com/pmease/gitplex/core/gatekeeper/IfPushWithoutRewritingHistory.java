package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;

@Editable(order=2000, icon="fa-ext fa-repo-lock", description="This gate keeper will be passed if the push "
		+ "operation does not rewrite history of target branch. Rewriting history of public branches "
		+ "is dangerous, and it happens when user forces a push without merging/rebasing with the "
		+ "branch head.")
public class IfPushWithoutRewritingHistory extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (!oldCommit.equals(ObjectId.zeroId()) && !newCommit.equals(ObjectId.zeroId())) {
			if (depot.isAncestor(oldCommit.name(), newCommit.name()))
				return passed(Lists.newArrayList("Push operation does not rewrite history"));
			else
				return failed(Lists.newArrayList("Push operation rewrites history"));
		} else {
			return ignored();
		}
	}

}
