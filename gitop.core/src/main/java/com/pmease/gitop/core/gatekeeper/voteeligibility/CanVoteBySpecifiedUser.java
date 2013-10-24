package com.pmease.gitop.core.gatekeeper.voteeligibility;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public class CanVoteBySpecifiedUser implements VoteEligibility {

    private final Long userId;
    
    public CanVoteBySpecifiedUser(User user) {
        this.userId = user.getId();
    }
    
    @Override
    public boolean canVote(User user, MergeRequest request) {
        return user.getId().equals(userId);
    }

}
