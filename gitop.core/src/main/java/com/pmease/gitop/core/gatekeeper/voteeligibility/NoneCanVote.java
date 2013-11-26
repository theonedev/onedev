package com.pmease.gitop.core.gatekeeper.voteeligibility;

import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public class NoneCanVote implements VoteEligibility {

    @Override
    public boolean canVote(User user, PullRequest request) {
        return false;
    }

}
