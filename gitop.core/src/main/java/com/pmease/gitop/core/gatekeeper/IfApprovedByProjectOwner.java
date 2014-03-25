package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;

@SuppressWarnings("serial")
@Editable(order=300, icon="icon-user", description=
		"This gate keeper will be passed if the commit is approved by owner of the project.")
public class IfApprovedByProjectOwner extends ApprovalGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	if (request.isNew())
    		return checkApproval(request.getSubmittedBy(), request.getTarget().getProject());
    	
        User projectOwner = request.getTarget().getProject().getOwner();

        Vote.Result result = projectOwner.checkVoteSince(request.getBaseUpdate());

        if (result == null) {
            request.inviteToVote(Sets.newHashSet(projectOwner), 1);
            return pending("To be approved by user '" + projectOwner.getName() + "'.",
                    new CanVoteBySpecifiedUser(projectOwner));
        } else if (result == Vote.Result.APPROVE) {
            return approved("Approved by user '" + projectOwner.getName() + "'.");
        } else {
            return disapproved("Rejected by user '" + projectOwner.getName() + "'.");
        }
    }

    private CheckResult checkApproval(User user, Repository project) {
		if (user.equals(project.getOwner()))
			return approved("Approved by project owner.");
		else
			return pending("Not approved by project owner.", new CanVoteBySpecifiedUser(project.getOwner()));
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkApproval(user, branch.getProject());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkApproval(user, branch.getProject());
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository project, String refName) {
		if (user.equals(project.getOwner()))
			return approved("Approved by project owner.");
		else
			return disapproved("Not approved by project owner.");
	}

}
