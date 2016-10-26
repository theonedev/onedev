package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;

@Editable(order=3100, icon="fa-ext fa-branch-merge", description="This gatekeeper will be passed if "
		+ "head commit of target branch is merged into source branch of the pull request")
public class TargetBranchIsMerged extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected GateCheckResult doCheckRequest(PullRequest request) {
		RevCommit requestHeadCommit = request.getHeadCommit();
		RevCommit branchHeadCommit = request.getTarget().getCommit();
		if (!GitUtils.isMergedInto(request.getTargetDepot().getRepository(), branchHeadCommit, requestHeadCommit)) {
			return failed(Lists.newArrayList("Please merge target branch into the source branch first"));
		} else {
			return passed(Lists.newArrayList("No more than one commit"));
		}
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return ignored();
	}

}
