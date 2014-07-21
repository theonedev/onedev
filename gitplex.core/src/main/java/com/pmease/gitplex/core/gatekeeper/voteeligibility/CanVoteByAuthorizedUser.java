package com.pmease.gitplex.core.gatekeeper.voteeligibility;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;

@SuppressWarnings("serial")
public class CanVoteByAuthorizedUser implements VoteEligibility {

    @Override
    public boolean canVote(User user, PullRequest request) {
        return user.asSubject().isPermitted(ObjectPermission.ofRepositoryWrite(request.getTarget().getRepository()));
    }

}
