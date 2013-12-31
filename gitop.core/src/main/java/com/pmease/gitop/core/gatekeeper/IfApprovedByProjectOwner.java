package com.pmease.gitop.core.gatekeeper;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
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
        User projectOwner = request.getTarget().getProject().getOwner();

        Vote.Result result = projectOwner.checkVoteSince(request.getBaseUpdate());

        if (result == null) {
            Gitop.getInstance(VoteInvitationManager.class).inviteToVote(request, Sets.newHashSet(projectOwner), 1);
            return pending("To be approved by user '" + projectOwner.getName() + "'.",
                    new CanVoteBySpecifiedUser(projectOwner));
        } else if (result.isAccept()) {
            return accepted("Approved by user '" + projectOwner.getName() + "'.");
        } else {
            return rejected("Rejected by user '" + projectOwner.getName() + "'.");
        }
    }

    private CheckResult checkBranch(User user, Branch branch) {
		if (user.equals(branch.getProject().getOwner()))
			return accepted("Approved by project owner.");
		else
			return pending("Not approved by project owner.", new CanVoteBySpecifiedUser(branch.getProject().getOwner()));
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, @Nullable String file) {
		return checkBranch(user, branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkBranch(user, branch);
	}

}
