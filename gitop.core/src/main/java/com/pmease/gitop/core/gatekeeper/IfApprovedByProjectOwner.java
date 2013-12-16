package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.VoteInvitationManager;
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
    public CheckResult doCheck(PullRequest request) {
        User projectOwner = request.getTarget().getProject().getOwner();

        Vote.Result result = projectOwner.checkVoteSince(request.getBaseUpdate());

        if (result == null) {
            if (request.getId() != null)
            	Gitop.getInstance(VoteInvitationManager.class).inviteToVote(request, Sets.newHashSet(projectOwner), 1);
    		String prefix;
    		if (request.getId() == null)
    			prefix = "Not ";
    		else
    			prefix = "To be ";
            return pending(prefix + "approved by user '" + projectOwner.getName() + "'.",
                    new CanVoteBySpecifiedUser(projectOwner));
        } else if (result.isAccept()) {
            return accepted("Approved by user '" + projectOwner.getName() + "'.");
        } else {
            return rejected("Rejected by user '" + projectOwner.getName() + "'.");
        }
    }

}
