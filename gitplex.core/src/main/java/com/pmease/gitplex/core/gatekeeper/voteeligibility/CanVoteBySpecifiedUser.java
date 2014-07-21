package com.pmease.gitplex.core.gatekeeper.voteeligibility;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class CanVoteBySpecifiedUser implements VoteEligibility {

    private final Long userId;
    
    public CanVoteBySpecifiedUser(User user) {
        this.userId = user.getId();
    }
    
    @Override
    public boolean canVote(User user, PullRequest request) {
        return user.getId().equals(userId);
    }

}
