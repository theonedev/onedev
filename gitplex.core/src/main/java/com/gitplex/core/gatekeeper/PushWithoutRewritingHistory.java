package com.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.google.common.collect.Lists;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(order=2000, icon="fa-ext fa-repo-lock", description="This gatekeeper will be passed if the push "
		+ "operation does not rewrite history of target branch. Rewriting history of public branches "
		+ "is dangerous, and it happens when user forces a push without merging/rebasing with the "
		+ "branch head")
public class PushWithoutRewritingHistory extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected GateCheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (!oldCommit.equals(ObjectId.zeroId()) && !newCommit.equals(ObjectId.zeroId())) {
			if (GitUtils.isMergedInto(depot.getRepository(), oldCommit, newCommit))
				return passed(Lists.newArrayList("Not trying to rewrite history"));
			else
				return failed(Lists.newArrayList("Trying to rewrite history"));
		} else {
			return ignored();
		}
	}

}
