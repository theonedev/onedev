package com.pmease.gitplex.core.gatekeeper.voteeligibility;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class NoneCanVote implements VoteEligibility {

    @Override
    public boolean canVote(User user, PullRequest request) {
        return false;
    }

}
