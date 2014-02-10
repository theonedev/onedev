package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.AbstractGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable(order=150, description="This gate keeper will be passed if the pull request is submitted with "
		+ "gerrit style patch, that is, only one commit is submitted for review at a time, and "
		+ "subsequent updates to the review should be done by amending previous commit.")
@SuppressWarnings("serial")
public class IfSubmitWithGerritStylePatch extends AbstractGateKeeper {

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		String branchHead = request.getTarget().getHeadCommit();
		String requestHead;
		if (request.isNew())
			requestHead = request.getSource().getHeadCommit();
		else
			requestHead = request.getLatestUpdate().getHeadCommit();
		if (request.getTarget().getProject().code().log(branchHead, requestHead, null, 0, 0).size() > 1) {
			return disapproved("Please squash/rebase your commits.");
		} else {
			return approved("No more than one commit.");
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
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return ignored();
	}

}
