package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Editable(order=3000, icon="fa-ext fa-file-diff", description="This gate keeper will be passed if the pull request "
		+ "is submitted with gerrit style patch, that is, only one commit is submitted for review at "
		+ "a time, and subsequent updates to the review should be done by amending previous commit.")
public class IfSubmitWithGerritStylePatch extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		String branchHead = request.getBaseCommitHash();
		String requestHead = request.getLatestUpdate().getHeadCommitHash();
		if (request.git().log(branchHead, requestHead, null, 0, 0, false).size() > 1) {
			return failed(Lists.newArrayList("Please squash/rebase your commits."));
		} else {
			return passed(Lists.newArrayList("No more than one commit."));
		}
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return ignored();
	}

}
