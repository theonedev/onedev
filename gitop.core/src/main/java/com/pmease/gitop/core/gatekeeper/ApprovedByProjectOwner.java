package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;
import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@SuppressWarnings("serial")
@Editable
public class ApprovedByProjectOwner extends AbstractGateKeeper {

    @Override
    public CheckResult check(PullRequest request) {
        User projectOwner = request.getTarget().getProject().getOwner();

        Vote.Result result = projectOwner.checkVoteSince(request.getBaseUpdate());

        if (result == null) {
            request.inviteToVote(Sets.newHashSet(projectOwner), 1);
            return pending("To be approved by user '" + projectOwner.getName() + "'.",
                    new CanVoteBySpecifiedUser(projectOwner));
        } else if (result.isAccept()) {
            return accepted("Approved by user '" + projectOwner.getName() + "'.");
        } else {
            return rejected("Rejected by user '" + projectOwner.getName() + "'.");
        }
    }

}
