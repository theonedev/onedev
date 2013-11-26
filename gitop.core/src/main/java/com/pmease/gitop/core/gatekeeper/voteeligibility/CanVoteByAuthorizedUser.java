package com.pmease.gitop.core.gatekeeper.voteeligibility;

import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;

@SuppressWarnings("serial")
public class CanVoteByAuthorizedUser implements VoteEligibility {

    @Override
    public boolean canVote(User user, PullRequest request) {
        return user.asSubject().isPermitted(ObjectPermission.ofProjectWrite(request.getTarget().getProject()));
    }

}
