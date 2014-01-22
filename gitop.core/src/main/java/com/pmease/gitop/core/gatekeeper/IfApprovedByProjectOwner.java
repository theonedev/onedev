package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
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
    	if (request.isNew())
    		return checkApproval(request.getSubmitter(), request.getTarget().getProject());
    	
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

    private CheckResult checkApproval(User user, Project project) {
		if (user.equals(project.getOwner()))
			return accepted("Approved by project owner.");
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
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		if (user.equals(project.getOwner()))
			return accepted("Approved by project owner.");
		else
			return rejected("Not approved by project owner.");
	}

}
