package com.pmease.gitop.model.gatekeeper.voteeligibility;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

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
