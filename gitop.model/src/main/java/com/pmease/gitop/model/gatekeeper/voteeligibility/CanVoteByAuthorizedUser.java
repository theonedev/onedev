package com.pmease.gitop.model.gatekeeper.voteeligibility;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;

@SuppressWarnings("serial")
public class CanVoteByAuthorizedUser implements VoteEligibility {

    @Override
    public boolean canVote(User user, PullRequest request) {
        return user.asSubject().isPermitted(ObjectPermission.ofProjectWrite(request.getTarget().getProject()));
    }

}
