package com.pmease.gitop.core.gatekeeper.voteeligibility;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public class NoneCanVote implements VoteEligibility {

    @Override
    public boolean canVote(User user, MergeRequest request) {
        return false;
    }

}
