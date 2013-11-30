package com.pmease.gitop.model.gatekeeper.voteeligibility;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
public class NoneCanVote implements VoteEligibility {

    @Override
    public boolean canVote(User user, PullRequest request) {
        return false;
    }

}
